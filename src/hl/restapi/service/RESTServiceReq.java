package hl.restapi.service;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import org.json.JSONObject;
import hl.common.http.RestApiUtil;

public class RESTServiceReq {

	public static String _RESTAPI_PLUGIN_IMPL_CLASSNAME = "restapi.plugin.implementation";
	public static String _RESTAPI_ECHO_JSONATTR_PREFIX	= "restapi.echo.jsonattr.prefix";
	public static String _RESTAPI_BASE_URL				= "restapi.baseurl";
	
	//
	protected String urlPath 						= null;
	protected HttpServletRequest httpServletReq		= null;
	//
	protected String reqInputContentType			= null;
	protected String reqInputContentData			= null;

	protected JSONObject jsonEchoAttrs 				= null;
	protected JSONObject jsonUrlPathParams 			= null;
	
	protected Map<String, String> mapConfigs		= null;
	

	//
	
	public RESTServiceReq(HttpServletRequest aReq, Map<String, String> aConfigMap)
	{
		this.httpServletReq = aReq;
		this.mapConfigs = aConfigMap;
			
		init(aReq, aConfigMap);
	}
	
	private void init(HttpServletRequest aReq, Map<String, String> aConfigMap)
	{
		this.urlPath = aReq.getPathInfo();  //without context root
		
		this.reqInputContentType = aReq.getContentType();
		this.reqInputContentData = RestApiUtil.getReqContent(aReq);
		
		this.jsonEchoAttrs = extractEchoAttrs(aReq, aConfigMap);
		
		String sBaseUrl = aConfigMap.get(_RESTAPI_BASE_URL);
		this.jsonUrlPathParams = extractPathParams(aReq.getPathInfo(), sBaseUrl);
	}
	///
	
	public Map<String, String> getConfigMap()
	{
		if(mapConfigs==null)
			return new HashMap<String, String>();
		return mapConfigs;
	}
	
	public void addToConfigMap(Map<String, String> aConfigMap)
	{
		if(aConfigMap!=null)
		{
			mapConfigs.putAll(aConfigMap);
		}
	}
	
	public void addToConfigMap(Properties aProp)
	{
		if(aProp!=null)
		{
			for(Object oPropKey : aProp.keySet())
			{
				String sPropKey = String.valueOf(oPropKey);
				addToConfigMap(sPropKey,aProp.getProperty(sPropKey));
			}
		}
	}
	
	public void addToConfigMap(String aKey, String aValue)
	{
		if(mapConfigs==null)
			mapConfigs = new HashMap<String, String>();
		mapConfigs.put(aKey, aValue);
	}
	
	public JSONObject getEchoJsonAttrs()
	{
		return jsonEchoAttrs;
	}
	
	public JSONObject getUrlPathParam()
	{
		return jsonUrlPathParams;
	}
	
	public void addUrlPathParam(String aParamName, String aParamValue)
	{
		if(jsonUrlPathParams==null)
			jsonUrlPathParams = new JSONObject();
		jsonUrlPathParams.put(aParamName, aParamValue);
	}

	public void addUrlPathParam(Map<String, String> aPathParamsMap)
	{
		if(aPathParamsMap!=null)
		{
			for(String sParamKey : aPathParamsMap.keySet())
			{
				String sParamValue = aPathParamsMap.get(sParamKey);
				addUrlPathParam(sParamKey, sParamValue);
			}
		}
	}
	
	public String getUrlPath()
	{
		return this.urlPath;
	}	

	public String getInputContentData()
	{
		return this.reqInputContentData;
	}
	
	public void setInputContentData(String aContentData)
	{
		this.reqInputContentData = aContentData;
	}

	public String getInputContentType()
	{
		return this.reqInputContentType;
	}
	
	public void setInputContentType(String aContentType)
	{
		this.reqInputContentType = aContentType;
	}		
	
	public String getHttpMethod()
	{
		return this.httpServletReq.getMethod();
	}
	
	public HttpServletRequest getHttpServletReq()
	{
		return this.httpServletReq;
	}
	
    private JSONObject extractEchoAttrs(HttpServletRequest aReq, Map<String, String> aConfigMap)
    {
    	JSONObject jsonAttrs = null;
		String sJsonAttrEchoPrefix = aConfigMap.get(RESTApiService._RESTAPI_ECHO_JSONATTR_PREFIX);
		if(sJsonAttrEchoPrefix!=null && sJsonAttrEchoPrefix.trim().length()>0)
		{
			jsonAttrs = new JSONObject();
			//
			if(this.reqInputContentData!=null && this.reqInputContentData.trim().startsWith("{"))
			{
				JSONObject jsonTmp = new JSONObject(this.reqInputContentData);
				for(String sKey : jsonTmp.keySet())
				{
					if(sKey.startsWith(sJsonAttrEchoPrefix))
					{
						jsonAttrs.put(sKey, jsonTmp.get(sKey));
					}
				}
			}
			
			// http headers
			Enumeration<String> e = aReq.getHeaderNames();
			while(e.hasMoreElements())
			{
				String sHeaderName = e.nextElement();
				if(sHeaderName.startsWith(sJsonAttrEchoPrefix))
				{
					jsonAttrs.put(sHeaderName, aReq.getHeader(sHeaderName));
				}
			}
			// query parameters
			e = aReq.getParameterNames();
			while(e.hasMoreElements())
			{
				String sParamName = e.nextElement();
				if(sParamName.startsWith(sJsonAttrEchoPrefix))
				{
					String sParamVal = aReq.getParameter(sParamName);
					jsonAttrs.put(sParamName, sParamVal);
				}
			}
			
		}
		return jsonAttrs;
    }
    
    private JSONObject extractPathParams(String aReqPathUrl, String aConfigPathUrl)
    {
    	JSONObject jsonParam = new JSONObject();
    	
    	
    	
    	
		return jsonParam;
    }
    
	
	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		sb.append("getPathInfo:").append(getUrlPath());
		sb.append("\n").append("HttpMethod:").append(getHttpMethod());
		sb.append("\n").append("InputContentType:").append(getInputContentType());
		sb.append("\n").append("InputContentData:").append(getInputContentData());
		//
		sb.append("\n").append("EchoAttrs:").append(getEchoJsonAttrs());
		
		return sb.toString();
	}
}
