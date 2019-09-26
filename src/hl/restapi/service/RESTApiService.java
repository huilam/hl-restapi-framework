package hl.restapi.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import hl.common.http.HttpResp;
import hl.common.http.RestApiUtil;
import hl.restapi.plugins.IServicePlugin;

public class RESTApiService extends HttpServlet {

	private static final long serialVersionUID = -6326475336541548927L;
	
	protected final static String TYPE_APP_JSON 	= "application/json"; 
	protected final static String TYPE_PLAINTEXT 	= "text/plain"; 
	
	private static RESTApiConfig apiConfig = new RESTApiConfig();

	
	private static String _VERSION = "0.0.7";
		
	public static final String GET 		= "GET";
	public static final String POST 	= "POST";
	public static final String DELETE	= "DELETE";
	public static final String PUT 		= "PUT";
	
	private static Map<String, Map<String, String>> mapMandatoryCache = new HashMap<String, Map<String, String>>();

    @Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException {
    	
    	boolean isAbout = GET.equals(request.getMethod()) 
    			&& "/about/framework".equals(request.getPathInfo());
    	
    	if(isAbout)
    	{
			try {
				RestApiUtil.processHttpResp(response, 
						HttpServletResponse.SC_OK, 
						TYPE_APP_JSON, getAbout().toString());
			} catch (IOException e) {
				throw new ServletException(e);
			}
    	}
    	else
    	{
    		processHttpMethods(request, response);
    	}
	}
    
    @Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException {
    	processHttpMethods(request, response);
	}

    @Override
	protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException {
    	processHttpMethods(request, response);
	}

    @Override
	protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException {
    	processHttpMethods(request, response);
	}
    
    protected JSONObject getAbout()
    {
    	JSONObject json = new JSONObject();
    	json.put("restapi.framework", _VERSION);
    	return json;
    }

    private void processHttpMethods(HttpServletRequest req, HttpServletResponse res) throws ServletException
    {
    	String sPathInfo 			= req.getPathInfo();  //{crudkey}/xx/xx
    	
    	JSONArray jsonArrErrors 	= new JSONArray();
    	
    	HttpResp httpReq = new HttpResp();
    	httpReq.setHttp_status(HttpServletResponse.SC_NOT_FOUND);
  	
		String[] sUrlPaths = RESTApiUtil.getUrlSegments(sPathInfo);
		Map<String, String> mapUrl = apiConfig.getMapLenUrls().get(sUrlPaths.length);
		
		String sRestApiKey = null;
		
		if(mapUrl!=null)
		{
			for(String sMapUrl : mapUrl.keySet())
			{
				sRestApiKey = mapUrl.get(sMapUrl);
				String[] sMapUrls = RESTApiUtil.getUrlSegments(sMapUrl);
				for(int i=0; i<sMapUrls.length; i++)
				{
					if(sMapUrls[i].startsWith("{") && sMapUrls[i].endsWith("}"))
					{
						//ok, path param
					}
					else if(!sMapUrls[i].equals(sUrlPaths[i]))
					{
						sRestApiKey = null;
						break;
					}
				}
				
				if(sRestApiKey!=null)
				{
					break;
				}
			}
		}
		
		
		if(sRestApiKey!=null)
		{
			//
			RESTServiceReq restReq = new RESTServiceReq(req, apiConfig.getConfig(sRestApiKey));
			restReq.setRestApiKey(sRestApiKey);

			Map<String, String> mapConfig = restReq.getConfigMap();
			
			//
			IServicePlugin plugin = null;
			try {
				plugin = getPlugin(mapConfig);

				if(!httpReq.hasErrors())
				{
					httpReq = checkMandatoryJSONAttr(restReq, httpReq);
				}
				if(!httpReq.hasErrors())
				{
					httpReq = doProxy(restReq, httpReq);
				}
				if(!httpReq.hasErrors())
				{
					httpReq = postProcess(plugin, restReq, httpReq);
				}

				///
				if(httpReq.hasErrors())
				{
					for(String sErrID : httpReq.getErrorMap().keySet())
					{
						JSONObject jsonErr = new JSONObject();
						jsonErr.put(sErrID, httpReq.getErrorMap().get(sErrID));
						jsonArrErrors.put(jsonErr);
					}
				}
				
			} catch (RESTApiException e) {
				
				JSONObject json = new JSONObject();
				json.put(e.getErrorCode(), e.getErrorMsg());

				jsonArrErrors.put(json);
				
				httpReq = handleException(plugin, restReq, httpReq, e);	
			}
			
			System.out.println("jsonArrErrors.length="+jsonArrErrors.length());
			
			if(jsonArrErrors.length()>0)
			{
				JSONObject jsonError = new JSONObject();
				jsonError.put("errors", jsonArrErrors);
				httpReq.setContent_type(TYPE_APP_JSON);
				httpReq.setContent_data(jsonError.toString());
				httpReq.setHttp_status(HttpServletResponse.SC_BAD_REQUEST);
			}
		}
		
		try {
			RestApiUtil.processHttpResp(res, httpReq, -1);
		} catch (IOException ex) {
			throw new ServletException(ex.getClass().getSimpleName(), ex);
		}
    }
        
    
    public HttpResp doProxy(
    		RESTServiceReq aRestReq, HttpResp aHttpResp) throws RESTApiException
    {
    	String sProxyUrl = getHttpMethodSpecifiedConfig(
				aRestReq, RESTApiConfig._KEY_PROXY_URL);
    	
    	if(sProxyUrl!=null && sProxyUrl.trim().length()>0)
    	{
    		try {
    			if(GET.equalsIgnoreCase(aRestReq.getHttpMethod()))
    			{
    				aHttpResp = RestApiUtil.httpGet(sProxyUrl);
    			}
    			else if(POST.equalsIgnoreCase(aRestReq.getHttpMethod()))
    			{
    				aHttpResp = RestApiUtil.httpPost(sProxyUrl, aRestReq.getInputContentType(), aRestReq.getInputContentData());
    			}
    			else if(DELETE.equalsIgnoreCase(aRestReq.getHttpMethod()))
    			{
    				aHttpResp = RestApiUtil.httpDelete(sProxyUrl, aRestReq.getInputContentType(), aRestReq.getInputContentData());
    			}
    			else if(PUT.equalsIgnoreCase(aRestReq.getHttpMethod()))
    			{
    				aHttpResp = RestApiUtil.httpPut(sProxyUrl, aRestReq.getInputContentType(), aRestReq.getInputContentData());
    			}
			} catch (IOException e) {
				throw new RESTApiException("ProxyError", e);
			}
    	}
    	
    	return aHttpResp;
    }
    
    public HttpResp checkMandatoryJSONAttr(
    		RESTServiceReq aRestReq, HttpResp aHttpResp) throws RESTApiException
    {
		List<String> listMandatory 	= new ArrayList<String>();

    	String sMandatoryJsonAttrs = getHttpMethodSpecifiedConfig(
										aRestReq, RESTApiConfig._KEY_MANDATORY_JSONATTRS);
    	
		if(sMandatoryJsonAttrs!=null && sMandatoryJsonAttrs.trim().length()>0)
		{
			System.out.println("sMandatoryJsonAttrs="+sMandatoryJsonAttrs);
			
			StringTokenizer tk = new StringTokenizer(sMandatoryJsonAttrs, ",");
			while(tk.hasMoreTokens())
			{
				String sJsonAttrName = tk.nextToken();
				listMandatory.add(sJsonAttrName.trim());
			}
					
			if(listMandatory.size()>0)
			{
				String sInputContent = aRestReq.getInputContentData();
				if(sInputContent!=null)
				{
					sInputContent = sInputContent.trim();
					boolean isJsonObj = sInputContent.startsWith("{") && sInputContent.endsWith("}");
					if(isJsonObj)
					{
						sInputContent = "[" + sInputContent + "]";
					}
					
					boolean isJsonArr = sInputContent.startsWith("[") && sInputContent.endsWith("]");
					if(isJsonArr)
					{
						JSONArray jsonArr = new JSONArray(sInputContent);
						
						for(int i=0; i<jsonArr.length(); i++)
						{
							JSONObject json = jsonArr.optJSONObject(i);
							
							for(String sMandatoryAttrKey : listMandatory)
							{
								if(!json.has(sMandatoryAttrKey))
								{
									aHttpResp.addToErrorMap(sMandatoryAttrKey, "MANDATORY");
								}
							}
						}
					}
				
				}
				
			}
		}
    	
    	return aHttpResp;
    }
    
    private String getHttpMethodSpecifiedConfig(RESTServiceReq aRestReq, String aConfigKey)
    {
    	if(aConfigKey==null)
    		return null;
    	
    	String sValue = null;
    	
    	Map<String, String> mapConfig = aRestReq.getConfigMap();
    	
    	if(aConfigKey.indexOf(RESTApiConfig._VAR_HTTP_METHOD)>-1)
    	{
    		String sTmpKey = aConfigKey.replaceAll("\\"+RESTApiConfig._VAR_HTTP_METHOD, "");
    		//System.out.println("sTmpKeyKey="+sTmpKey);
    		
    		sValue = mapConfig.get(sTmpKey);
    	
    		if(sValue==null)
    		{
	    		sTmpKey = sTmpKey + "."+aRestReq.getHttpMethod().toUpperCase();
	    		//System.out.println("sTmpKey="+sTmpKey);
	    		sValue = mapConfig.get(sTmpKey);
    		}
    	}
    	return sValue;
    }
    
    
    public HttpResp postProcess(
    		IServicePlugin aPlugin, 
    		RESTServiceReq aRestReq, HttpResp aHttpResp) throws RESTApiException
    {
    	if(aRestReq.getEchoJsonAttrs()!=null)
    	{
    		if(aHttpResp.getContent_data()!=null && aHttpResp.getContent_data().startsWith("{"))
    		{
    			JSONObject jsonEcho = aRestReq.getEchoJsonAttrs();
    			JSONObject json = new JSONObject(aHttpResp.getContent_data());
    			//
    			for(String sEchoKey : jsonEcho.keySet())
    			{
    				Object oEchoVal = jsonEcho.get(sEchoKey);
    				json.put(sEchoKey, oEchoVal);
    			}
    			aHttpResp.setContent_data(json.toString());
    		}
    	}
    	
    	if(aPlugin==null)
    		return aHttpResp;
    	return aPlugin.postProcess(aRestReq, aHttpResp);
    }
    
    public HttpResp handleException(
    		IServicePlugin aPlugin, 
    		RESTServiceReq aRestReq , HttpResp aHttpResp, RESTApiException aException)
    {
    	if(aPlugin==null)
    		return aHttpResp;
    	return aPlugin.handleException(aRestReq , aHttpResp, aException);
    }
    

    
    private IServicePlugin getPlugin(Map<String, String> aMapConfig) throws RESTApiException
    {
		IServicePlugin plugin = null;
		String sPluginClassName = aMapConfig.get(RESTApiConfig._KEY_PLUGIN_CLASSNAME);
    	if(sPluginClassName!=null && sPluginClassName.trim().length()>0)
    	{
	    	try {
				plugin = (IServicePlugin) Class.forName(sPluginClassName).newInstance();
			} catch (InstantiationException e) {
				throw new RESTApiException(RESTApiConfig.ERRCODE_PLUGINEXCEPTION, e);
			} catch (IllegalAccessException e) {
				throw new RESTApiException(RESTApiConfig.ERRCODE_PLUGINEXCEPTION, e);
			} catch (ClassNotFoundException e) {
				throw new RESTApiException(RESTApiConfig.ERRCODE_PLUGINEXCEPTION, e);
			}
    	}
    	return plugin;
    }

    
}
