package hl.restapi.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import hl.common.http.HttpResp;
import hl.common.http.RestApiUtil;

public class RESTApiService extends HttpServlet {

	private static final long serialVersionUID = -6326475336541548927L;
	
	protected final static String TYPE_APP_JSON 	= "application/json"; 
	protected final static String TYPE_PLAINTEXT 	= "text/plain"; 

	protected static String _RESTAPI_PLUGIN_IMPL_CLASSNAME 	= "restapi.plugin.implementation";
	protected static String _RESTAPI_ECHO_JSONATTR_PREFIX	= "restapi.echo.jsonattr.prefix";
	
	private static String _VERSION = "0.0.1";
		
	public static final String GET 		= "GET";
	public static final String POST 	= "POST";
	public static final String DELETE	= "DELETE";
	public static final String PUT 		= "PUT";

	public RESTApiService() {
        super();
    }
    
    @Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException {

    	boolean isAbout = request.getMethod().equals(GET) && request.getPathInfo().equals("/about");
    	if(isAbout)
    	{
			try {
				RestApiUtil.processHttpResp(response, 
						HttpServletResponse.SC_NO_CONTENT, 
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
    	
    	JSONObject jsonResult 		= null;
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
		String[] sPaths = getUrlSegments(sPathInfo);
		
		Map<String, String> mapCrudConfig = null;
		if(mapCrudConfig==null)
			mapCrudConfig = new HashMap<String, String>();
		//
		RESTServiceReq restReq = new RESTServiceReq(req, mapCrudConfig);

		//
		IServicePlugin plugin = null;
		try {
			plugin = getPlugin(mapCrudConfig);
			restReq = preProcess(plugin, restReq);

			if(GET.equalsIgnoreCase(restReq.getHttpMethod()))
			{
			}
			else if(POST.equalsIgnoreCase(restReq.getHttpMethod()))
			{
			}
			else if(PUT.equalsIgnoreCase(restReq.getHttpMethod()))
			{				
			}
			else if(DELETE.equalsIgnoreCase(restReq.getHttpMethod()))
			{
			}
			///////////////////////////
			
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
		
		try {
			RestApiUtil.processHttpResp(res, httpReq.getHttp_status(), httpReq.getContent_type(), httpReq.getContent_data());
		} catch (IOException ex) {
			throw new ServletException(ex.getClass().getSimpleName(), ex);
		}
    }
        
    public RESTServiceReq preProcess(
    		IServicePlugin aPlugin, 
    		RESTServiceReq restReq) 
    {
    	if(aPlugin==null)
    		return restReq;
    	return aPlugin.preProcess(restReq);
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
    
	public static String[] getUrlSegments(String aURL)
	{
		if(aURL==null)
			return new String[]{};
		
		return aURL.trim().substring(1).split("/");
	}
    
    private IServicePlugin getPlugin(Map<String, String> aMapCrudConfig) throws RESTApiException
    {
		IServicePlugin plugin = null;
		String sPluginClassName = aMapCrudConfig.get(_RESTAPI_PLUGIN_IMPL_CLASSNAME);
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
