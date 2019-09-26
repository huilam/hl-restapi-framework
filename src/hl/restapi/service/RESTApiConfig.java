/*
 Copyright (c) 2017 onghuilam@gmail.com
 
 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:
 The above copyright notice and this permission notice shall be included in all
 copies or substantial portions of the Software.
 The Software shall be used for Good, not Evil.
 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 SOFTWARE.
 
 */

package hl.restapi.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import hl.common.PropUtil;


public class RESTApiConfig {
	
	public static String _PROP_FILENAME 			= "restapi.properties";
	//
	public static String _KEY_PLUGIN_CLASSNAME		= "plugin.implementation";
	public static String _KEY_ECHO_ATTR_PREFIX		= "optional.echo.jsonattr.prefix";
	public static String _KEY_MAPPED_URL			= "optional.mapped.url";
	
	public static String _KEY_JSONATTR_MANDATORY_CHECK	= "optional.<HTTP_METHOD>.jsonattr.mandatory.check";
	
	//
	public static String ERRCODE_PLUGINEXCEPTION	= "plugin_exception";
	public static String ERRCODE_INVALIDFORMAT		= "invalid_format_exception";
	
	//
	private static Map<Integer, Map<String, String>> mapLenUrls = new HashMap<Integer, Map<String, String>>();
	private static Pattern pattMappedUrlKey 	= Pattern.compile("restapi\\.(.+?)\\."+RESTApiConfig._KEY_MAPPED_URL); 	
	
	private static Pattern pattRestApiKey = Pattern.compile("restapi\\.(.+?)\\.(.+)"); 	
	private Map<String, Properties> mapConfigs = null;
	//

	public RESTApiConfig(String aPropFileName)
	{
		init(aPropFileName);
	}
	
	public RESTApiConfig() 
	{
		init(null);
	}
	
	public void init(String aPropFilename) 
	{		
		mapConfigs = new HashMap<String, Properties>();
		
		if(aPropFilename==null)
			aPropFilename = _PROP_FILENAME;
		
		Properties props = null;
		if(aPropFilename!=null && aPropFilename.trim().length()>0)
		{
			try {
				props = PropUtil.loadProperties(aPropFilename);
			} catch (IOException e) {
				props = null;
			}
		}
		
		for(Object oKey : props.keySet())
		{
			Matcher m = pattRestApiKey.matcher(oKey.toString());
			if(m.find())
			{
				String sApiKey = m.group(1);
				String sPropKey = m.group(2);
				
				Properties propApi = getConfig(sApiKey);
				
				if(propApi==null)
					propApi = new Properties();
				
				propApi.put(sPropKey, props.getProperty(oKey.toString()));
				
				addConfig(sApiKey, propApi);
			}
		}
		
		addUrlMapping(props);
	}
	
	public void addConfig(String sConfigKey, Properties aProp)
	{
		mapConfigs.put(sConfigKey, aProp);
	}
	
	public Properties getConfig(String sConfigKey)
	{
		return mapConfigs.get(sConfigKey);
	}
	
	public Map<String, Properties> getAllConfig()
	{
		return mapConfigs;
	}
	
	public Map<Integer, Map<String,String>> getMapLenUrls()
	{
		return mapLenUrls;
	}
	
    private static void addUrlMapping(Properties aProps)
    {
    	if(aProps==null)
    		return;
    	    	
        for(Object oKey : aProps.keySet())
        {
        	String sKey = oKey.toString();
        	if(sKey.endsWith("."+RESTApiConfig._KEY_MAPPED_URL))
        	{
        		Matcher m = pattMappedUrlKey.matcher(sKey);
	        	if(m.find())
	        	{
	        		String sRestApiKey = m.group(1);
	        		String sURL = aProps.getProperty(sKey);
	        		if(sURL!=null)
	        		{
	        			String[] sURLs = RESTApiUtil.getUrlSegments(sURL);
	        			int iUrlSeg = sURLs.length;
	        			if(iUrlSeg>0)
	        			{
	        				 Map<String, String> mapUrl = mapLenUrls.get(iUrlSeg);
	        				 if(mapUrl==null)
	        				 {
	        					 mapUrl = new HashMap<String, String>();
	        				 }
	        				 mapUrl.put(sURL, sRestApiKey);
	        				 
	        				 mapLenUrls.put(iUrlSeg, mapUrl);
	        			}	        			
	        		}
	        	}
        	}
        }    	
    }

}