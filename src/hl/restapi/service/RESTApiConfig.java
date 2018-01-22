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
import hl.common.PropUtil;


public class RESTApiConfig {
	
	public static String _PROP_FILENAME 			= "restapi.properties";
	//
	public static String ERRCODE_PLUGINEXCEPTION	= "plugin_exception";
	public static String ERRCODE_INVALIDFORMAT		= "invalid_format_exception";
	
	//
	private Map<String, Properties> mapConfigs = null;
	//

	public RESTApiConfig(String aPropFileName) throws IOException
	{
		init(aPropFileName);
	}
	
	public RESTApiConfig() throws IOException
	{
		init(null);
	}
	
	public void init(String aPropFilename) throws IOException
	{		
		mapConfigs = new HashMap<String, Properties>();
		
		Properties props = null;
		if(aPropFilename!=null && aPropFilename.trim().length()>0)
		{
			props = PropUtil.loadProperties(aPropFilename);
		}
		
		/////////
		if(props==null || props.size()==0)
		{
			props = PropUtil.loadProperties(_PROP_FILENAME);
		}
		
	}
	
	public Properties getConfig(String sConfigKey)
	{
		return mapConfigs.get(sConfigKey);
	}
}