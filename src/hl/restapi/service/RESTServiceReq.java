package hl.restapi.service;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import jakarta.servlet.http.HttpServletRequest;
import org.json.JSONObject;
import hl.common.http.RestApiUtil;

public class RESTServiceReq {

	protected String restApiKey						= null;
	//
	protected String reqUniqueID					= null;
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
	
	public RESTServiceReq(HttpServletRequest aReq, Properties aConfigProp)
	{
		if(aConfigProp==null)
			aConfigProp = new Properties();
		
		Map<String, String> mapTemp = new HashMap<String,String>();		
		for(Object oKey : aConfigProp.keySet())
		{
			String sKey = oKey.toString();
			mapTemp.put(sKey, aConfigProp.getProperty(sKey));
		}	
		init(aReq, mapTemp);
	}

	public RESTServiceReq(HttpServletRequest aReq, Map<String, String> aConfigMap)
	{
		init(aReq, aConfigMap);
	}
	
	private void init(HttpServletRequest aReq, Map<String, String> aConfigMap)
	{
		this.reqUniqueID = String.valueOf(System.nanoTime());
		this.httpServletReq = aReq;
		
    	if(aReq.getCharacterEncoding()==null || aReq.getCharacterEncoding().trim().length()==0)
    	{
    		try {
    			aReq.setCharacterEncoding("UTF-8");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}

		this.mapConfigs = new HashMap<String,String>();
		if(aConfigMap!=null)
		{
			this.mapConfigs.putAll(aConfigMap);
		}
		
		this.urlPath = aReq.getPathInfo();  //without context root
		
		this.reqInputContentType = aReq.getContentType();
		this.reqInputContentData = RestApiUtil.getReqContent(aReq);
		//
		String sJsonAttrEchoPrefix = this.mapConfigs.get(RESTApiConfig._KEY_ECHO_ATTR_PREFIX);
		if(sJsonAttrEchoPrefix!=null)
		{
			this.jsonEchoAttrs = RESTApiUtil.extractEchoAttrs(aReq, this.reqInputContentData, sJsonAttrEchoPrefix);
		}
		//
		String sBaseUrl = this.mapConfigs.get(RESTApiConfig._KEY_MAPPED_URL);
		if(sBaseUrl!=null)
		{
			this.jsonUrlPathParams = RESTApiUtil.extractPathParams(aReq, sBaseUrl);
		}
	}
	///
	
	public String getRestApiKey()
	{
		return this.restApiKey;
	}

	public void setRestApiKey(String aRestApiKey)
	{
		this.restApiKey = aRestApiKey;
	}
	
	public String getReqUniqueID()
	{
		return this.reqUniqueID;
	}

	public void setReqUniqueID(String aUniqueID)
	{
		this.reqUniqueID = aUniqueID;
	}
	
	public Map<String, String> getConfigMap()
	{
		if(this.mapConfigs==null)
			return new HashMap<String, String>();
		return this.mapConfigs;
	}
	
	public void addToConfigMap(Map<String, String> aConfigMap)
	{
		if(aConfigMap!=null)
		{
			this.mapConfigs.putAll(aConfigMap);
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
	
	public String getUrlPathParam(String aParamName)
	{
		if(jsonUrlPathParams==null)
			return null;
		
		if(jsonUrlPathParams.has(aParamName))
			return jsonUrlPathParams.getString(aParamName);
		else
			return null;
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
