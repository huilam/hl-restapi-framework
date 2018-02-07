package hl.restapi.service;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Enumeration;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

import org.json.JSONObject;

public class RESTApiUtil extends HttpServlet {

	private static final long serialVersionUID = -4060309439820494541L;

	public static String appendSuffix(String aString, String aSuffix)
    {
    	if(!aString.endsWith(aSuffix))
    	{
    		return aString + aSuffix;
    	}
    	else
    	{
    		return aString;
    	}
    }
    
	public static String[] getUrlSegments(String aURL)
	{
		if(aURL==null)
			return new String[]{};
		
		return aURL.trim().substring(1).split("/");
	}
	
	public static String urldecode(String aUrlString) 
	{
		try {
			return URLDecoder.decode(aUrlString, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return aUrlString;
	}
	
	public static String urlEncode(String aUrlString) 
	{
		try {
			return URLEncoder.encode(aUrlString, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return aUrlString;
	}
	
	
    public static JSONObject extractPathParams(HttpServletRequest aReq, String aConfigBaseUrl)
    {
    	JSONObject jsonParam = null;
    	String[] sUrlPaths = RESTApiUtil.getUrlSegments(aReq.getPathInfo());
    	String[] sCfgBaseUrls = RESTApiUtil.getUrlSegments(aConfigBaseUrl);
    	
    	if(sUrlPaths.length==sCfgBaseUrls.length)
    	{
    		jsonParam = new JSONObject();
    		for(int i=0; i<sCfgBaseUrls.length; i++)
    		{
    			String sSeg = sCfgBaseUrls[i];
    			if(sSeg.startsWith("{") && sSeg.endsWith("}"))
    			{
    				String sParamName = sSeg.substring(1, sSeg.length()-1);
    				String sParamValue = sUrlPaths[i];
    				jsonParam.put(sParamName, sParamValue);
    			}
    		}
    		
    		if(jsonParam.keySet().size()==0)
    		{
    			jsonParam = null; 
    		}
    	}
		return jsonParam;
    }
    
    public static JSONObject extractEchoAttrs(HttpServletRequest aReq, String aInputContent, String aEchoAttrPrefix)
    {
    	JSONObject jsonAttrs = null;
		if(aEchoAttrPrefix!=null && aEchoAttrPrefix.trim().length()>0)
		{
			jsonAttrs = new JSONObject();
			
			String sJsonInput = aInputContent;
			if(sJsonInput==null)
				return null;
			
			//
			sJsonInput = sJsonInput.trim();
			if(sJsonInput.startsWith("{") && sJsonInput.endsWith("}"))
			{
				JSONObject jsonTmp = new JSONObject(sJsonInput);
				for(String sKey : jsonTmp.keySet())
				{
					if(sKey.startsWith(aEchoAttrPrefix))
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
				if(sHeaderName.startsWith(aEchoAttrPrefix))
				{
					jsonAttrs.put(sHeaderName, aReq.getHeader(sHeaderName));
				}
			}
			// query parameters
			e = aReq.getParameterNames();
			while(e.hasMoreElements())
			{
				String sParamName = e.nextElement();
				if(sParamName.startsWith(aEchoAttrPrefix))
				{
					String sParamVal = aReq.getParameter(sParamName);
					jsonAttrs.put(sParamName, sParamVal);
				}
			}
			
		}
		return jsonAttrs;
    }       
    
}
