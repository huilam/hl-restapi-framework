package hl.restapi.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import hl.common.CommonException;
import hl.common.http.HttpResp;
import hl.common.http.RestApiUtil;
import hl.common.system.CommonInfo;
import hl.common.system.Windows;
import hl.restapi.plugins.IServicePlugin;

public class RESTApiService extends HttpServlet {

	private static final long serialVersionUID = -6326475336541548927L;
	
	protected final static String TYPE_APP_JSON 	= "application/json"; 
	protected final static String TYPE_PLAINTEXT 	= "text/plain"; 
	protected final static String TYPE_OCTET_STREAM = "octet-stream";
	
	private static RESTApiConfig apiConfig = new RESTApiConfig();

	private static Logger logger = Logger.getLogger(RESTApiService.class.getName());
	
	private static String _VERSION = "0.1.0";
		
	public static final String GET 		= "GET";
	public static final String POST 	= "POST";
	public static final String DELETE	= "DELETE";
	public static final String PUT 		= "PUT";
	
	private static Map<String, List<String>> mapMandatoryCache = new HashMap<String, List<String>>();
	private static Pattern pattDebugMode = Pattern.compile("/about/framework/debug/(.+?)/(true|false)");

    @Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException {
    	
    	String sHttpMethod = request.getMethod();
    	String sPath = request.getPathInfo();

    	if(sPath==null)
    		sPath = "";
    	
    	if(sPath.endsWith("/"))
    		sPath = sPath.substring(0, sPath.length()-1);
    	
    	boolean isAbout = GET.equals(sHttpMethod) && sPath.startsWith("/about/framework");
    	
    	if(isAbout)
    	{
    		JSONObject jsonAbout = getAbout();
    		 
    		if(sPath.startsWith("/about/framework/debug"))
    		{
	        	Matcher m = pattDebugMode.matcher(sPath);
	        	if(m.find())
	        	{
	        		String sApiKey = m.group(1);
	        		boolean isDebug = "true".equalsIgnoreCase(m.group(2));
	        		apiConfig.setDebug(sApiKey, isDebug);
	        	}
        		try {
    				RestApiUtil.processHttpResp(
    						response, 
    						HttpServletResponse.SC_OK, 
    						TYPE_APP_JSON, 
    						getDebugInfo().toString());
    			} catch (IOException e) {
    				throw new ServletException(e);
    			}  			
    		}
    		else if(sPath.equals("/about/framework/sysinfo"))
    		{	
    			jsonAbout.put("SysInfo", getSysInfoJson());
    		}
    		
    		else if(sPath.startsWith("/about/framework/loglevel/"))
    		{
    			if(sPath.endsWith("/DEBUG"))
    			{
    				logger.setLevel(Level.FINEST);
    			}
    			else if(sPath.endsWith("/INFO"))
    			{
    				logger.setLevel(Level.INFO);
    			}
    			else if(sPath.endsWith("/OFF"))
    			{
    				logger.setLevel(Level.OFF);
    			}
    		}
    		
			try {
				RestApiUtil.processHttpResp(
						response, 
						HttpServletResponse.SC_OK, 
						TYPE_APP_JSON, 
						jsonAbout.toString());
			} catch (IOException e) {
				throw new ServletException(e);
			}
    	}
    	else
    	{	
    		processHttpMethods(request, response);
    	}
	}
    
    private JSONObject getDebugInfo()
    {
    	JSONObject json = new JSONObject();
    	
		Map<String, Properties> mapAll = apiConfig.getAllConfig();
    	for(String apiKey : mapAll.keySet())
    	{
    		Properties prop = mapAll.get(apiKey);
    		boolean isDebug = Boolean.parseBoolean((String)prop.get(RESTApiConfig._KEY_DEBUG));
        	json.put(apiKey+"."+RESTApiConfig._KEY_DEBUG, isDebug);
    	}
    	
    	return json;
    }
    
    private static JSONObject getSysInfoJson()
    {
    	JSONObject json = new JSONObject();
    	
    	JSONObject jsonWin = Windows.getSystemInfo();
    	if(jsonWin!=null && jsonWin.length()>0)
    	{
    		json.put("windows", jsonWin);
    	}
    	
    	json.put("jvm", CommonInfo.getJDKInfo());
    	json.put("storage", CommonInfo.getDiskInfo());

    	json.put("system.environment", CommonInfo.getEnvProperties());
    	json.put("system.properties", CommonInfo.getSysProperties());
    	
    	return json;
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
    
    private boolean serveWebContent(Properties apiProp, HttpServletRequest req, HttpServletResponse res)
    {
    	if(!"true".equalsIgnoreCase(apiProp.getProperty(RESTApiConfig._KEY_STATIC_WEB)))
    		return false;
    	
    	return RestApiUtil.serveStaticWeb(req, res);
    }

    private String getRestApiKey(final HttpServletRequest req)
    {
    	
    	if(req==null)
    		return null;

    	String sUrlPath = req.getPathInfo();
    	
    	if(sUrlPath==null)
    		return null;
    	
		File f = RestApiUtil.getWebContentAsFile(req);
		boolean isWebContent = (f!=null) && f.isFile();
    	
       	String sRestApiKey = null;
       	
       	String[] sUrlPaths = RESTApiUtil.getUrlSegments(sUrlPath);
		int iUrlLen = sUrlPaths.length;
    			
		
		while(sRestApiKey==null && iUrlLen>0)
		{
		
			Map<String, String> mapUrl = apiConfig.getMapLenUrls().get(iUrlLen);
			
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
			
			if(isWebContent)
			{
				iUrlLen--;
			}
			else
			{
				break;
			}
			
		}
		return sRestApiKey;
    }
    
    private void processHttpMethods(HttpServletRequest req, HttpServletResponse res) throws ServletException
    {
    	List<CommonException> listException = new ArrayList<CommonException>();
    	
    	boolean isDebug  = false;
    	HttpResp httpReq = new HttpResp();
    	httpReq.setHttp_status(HttpServletResponse.SC_NOT_FOUND);
  	
		String sRestApiKey 	= getRestApiKey(req);
		long  lReqStartTime = System.currentTimeMillis();
		String sReqUniqueID = null;

		if(sRestApiKey!=null)
		{
			//
			Properties propApiConfig = apiConfig.getConfig(sRestApiKey);
 			if(serveWebContent(propApiConfig, req, res))
 			{
 				return;
 			}
			
 			RESTServiceReq restReq = new RESTServiceReq(req, propApiConfig);
			restReq.setRestApiKey(sRestApiKey);
			
			sReqUniqueID = RESTApiUtil.getReqUniqueId(restReq);
			restReq.setReqUniqueID(sReqUniqueID);

 			isDebug = apiConfig.isDebug(sRestApiKey) || logger.isLoggable(Level.FINE);
 			if(isDebug)
 			{
 				logger.info("[DEBUG] rid:"+sReqUniqueID+" "+sRestApiKey+".start - "+req.getMethod()+" "+req.getPathInfo());
 			}

 			
 			Map<String, String> mapConfig = restReq.getConfigMap();
			//
			IServicePlugin plugin = null;
			try {
				
				restReq.setReqUniqueID(sReqUniqueID);
				
				plugin = getPlugin(mapConfig);
				
				long lPluginStartTime = System.currentTimeMillis();
				
				if(isDebug)
				{
					logger.info("[DEBUG] rid:"+restReq.getReqUniqueID()+" "+restReq.getRestApiKey()+".plugin:"+plugin.getClass().getSimpleName()+".plugin.start");
				}				

				if(!httpReq.hasErrors())
				{
					httpReq = checkMandatoryJSONAttr(restReq, httpReq);
				}
				if(!httpReq.hasErrors())
				{
					httpReq = doForwardProxy(restReq, httpReq);
				}
				if(!httpReq.hasErrors())
				{
					httpReq = postProcess(plugin, restReq, httpReq);
				}
				
				if(isDebug)
				{
					long lElapsed = System.currentTimeMillis()-lPluginStartTime;
					logger.info("[DEBUG] rid:"+restReq.getReqUniqueID()+" "+restReq.getRestApiKey()+".plugin:"+plugin.getClass().getSimpleName()+".plugin.end - status:"+httpReq.getHttp_status()+" "+lElapsed+"ms");
				}


			} catch (RESTApiException e) {
				
				try {
					httpReq = handleException(plugin, restReq, httpReq, e);
				} catch (RESTApiException e1) {
					httpReq.addToErrorMap(e1.getErrorCode(), e1.getMessage());
				}
			}
			finally
			{
				///
				if(httpReq.hasErrors())
				{
					Map<String, String> map = httpReq.getErrorMap();
					for(String sErrID : map.keySet())
					{
						String sErrReason = map.get(sErrID);
						listException.add(new RESTApiException(sErrID, sErrReason));
					}
				}
				
			}
			
			if(listException.size()>0)
			{
				JSONArray jsonArrErrors = new JSONArray();
				
				for(CommonException ce : listException)
				{
					JSONObject jsonE = new JSONObject();
					jsonE.put(ce.getErrorCode(), ce.getErrorMsg());
					jsonArrErrors.put(jsonE);
				}
				
				JSONObject jsonError = new JSONObject();
				jsonError.put("errors", jsonArrErrors);
				httpReq.setContent_type(TYPE_APP_JSON);
				httpReq.setContent_data(jsonError.toString());
				httpReq.setHttp_status(HttpServletResponse.SC_BAD_REQUEST);
			}
		}
		
		if(isDebug)
		{
			long lTotalElapsedMs = System.currentTimeMillis()-lReqStartTime;
			logger.info("[DEBUG] rid:"+sReqUniqueID+" "+sRestApiKey+".end - status:"+httpReq.getHttp_status()+" "+lTotalElapsedMs+"ms");
		}
		
		try {
			RestApiUtil.processHttpResp(res, httpReq, -1);
		} catch (IOException ex) {
			throw new ServletException(ex.getClass().getSimpleName(), ex);
		}
    }
        
    
    public HttpResp doForwardProxy(
    		RESTServiceReq aRestReq, HttpResp aHttpResp) throws RESTApiException
    {
    	String sProxyUrl = getHttpMethodSpecifiedConfig(
				aRestReq, RESTApiConfig._KEY_PROXY_URL);
    	
    	if(sProxyUrl!=null && sProxyUrl.trim().length()>0)
    	{
    		//Path param
    		String sTmpProxyUrl = sProxyUrl.trim();
    		JSONObject jsonPathParam = aRestReq.getUrlPathParam();
    		if(jsonPathParam!=null)
    		{
    			for(String sParamKey : jsonPathParam.keySet())
    			{
    				String sReplaceStr = "\\{"+sParamKey+"\\}";
    				String sReplaceVal = jsonPathParam.getString(sParamKey);
    				sTmpProxyUrl = sTmpProxyUrl.replaceAll(sReplaceStr, sReplaceVal);
    			}
    		}
    		sProxyUrl = sTmpProxyUrl;
    		
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
			//System.out.println("sMandatoryJsonAttrs="+sMandatoryJsonAttrs);
			
			listMandatory = mapMandatoryCache.get(sMandatoryJsonAttrs);
			
			if(listMandatory==null || listMandatory.size()==0)
			{
				listMandatory = new ArrayList<String>();
				StringTokenizer tk = new StringTokenizer(sMandatoryJsonAttrs, ",");
				while(tk.hasMoreTokens())
				{
					String sJsonAttrName = tk.nextToken();
					listMandatory.add(sJsonAttrName.trim());
				}
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
									aHttpResp.addToErrorMap(sMandatoryAttrKey, RESTApiConfig.ERRCODE_MANDATORY);
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
    		RESTServiceReq aRestReq , HttpResp aHttpResp, RESTApiException aException) throws RESTApiException 
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
