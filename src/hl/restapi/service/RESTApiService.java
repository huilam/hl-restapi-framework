package hl.restapi.service;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;
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

	
	private static String _VERSION = "0.0.4";
		
	public static final String GET 		= "GET";
	public static final String POST 	= "POST";
	public static final String DELETE	= "DELETE";
	public static final String PUT 		= "PUT";

    @Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException {

    	boolean isAbout = request.getMethod().equals(GET) && request.getPathInfo().equals("/about");
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
    
    private JSONObject getAbout()
    {
    	JSONObject json = new JSONObject();
    	json.put("restapi.framework", _VERSION);
    	return json;
    }

    private void processHttpMethods(HttpServletRequest req, HttpServletResponse res) throws ServletException
    {
    	String sPathInfo 			= req.getPathInfo();  //{crudkey}/xx/xx
    	
    	JSONObject jsonErrors 		= new JSONObject();
    	
    	HttpResp httpReq = new HttpResp();
    	httpReq.setHttp_status(HttpServletResponse.SC_NOT_FOUND);
 
/*
System.out.println("sReqUri:"+sReqUri);
System.out.println("sPathInfo:"+sPathInfo);
System.out.println("sHttpMethod:"+sHttpMethod);
System.out.println("sInputContentType:"+sInputContentType);
System.out.println("sInputData:"+sInputData);
System.out.println();
*/    	
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
			Properties propConfig = apiConfig.getConfig(sRestApiKey);
			if(propConfig==null)
				propConfig = new Properties();
			//
			RESTServiceReq restReq = new RESTServiceReq(req, propConfig);

			//
			IServicePlugin plugin = null;
			try {
				plugin = getPlugin(propConfig);

				httpReq = postProcess(plugin, restReq, httpReq);
				
			} catch (RESTApiException e) {

				JSONArray jArrErrors = new JSONArray();
				
				if(jsonErrors.has("errors"))
					jArrErrors = jsonErrors.getJSONArray("errors");
				
				jArrErrors.put(e.getErrorCode()+" : "+e.getErrorMsg());
				
				httpReq.setContent_type(TYPE_APP_JSON);
				httpReq.setContent_data(jArrErrors.toString());
				httpReq.setHttp_status(HttpServletResponse.SC_BAD_REQUEST);
				httpReq.setHttp_status_message(e.getErrorMsg());
				
				httpReq = handleException(plugin, restReq, httpReq, e);
			}
			
		}
		
		
		try {
			RestApiUtil.processHttpResp(res, httpReq.getHttp_status(), httpReq.getContent_type(), httpReq.getContent_data());
		} catch (IOException ex) {
			throw new ServletException(ex.getClass().getSimpleName(), ex);
		}
    }
        
    
    public HttpResp postProcess(
    		IServicePlugin aPlugin, 
    		RESTServiceReq aRestReq, HttpResp aHttpResp)
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
    

    
    private IServicePlugin getPlugin(Properties aProp) throws RESTApiException
    {
		IServicePlugin plugin = null;
		String sPluginClassName = aProp.getProperty(RESTApiConfig._KEY_PLUGIN_CLASSNAME);
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
