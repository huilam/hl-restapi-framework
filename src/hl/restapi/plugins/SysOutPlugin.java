package hl.restapi.plugins;

import hl.common.http.HttpResp;
import hl.restapi.service.RESTApiException;
import hl.restapi.service.RESTServiceReq;

public class SysOutPlugin implements IServicePlugin {

	@Override
	public HttpResp postProcess(RESTServiceReq aRestReq, HttpResp aHttpResp) {
		System.out.println();
		System.out.println("[ postProcess ]");
		System.out.println("input:");
		System.out.println("- httpMethod="+aRestReq.getHttpMethod());
		System.out.println("- contentType="+aRestReq.getInputContentType());
		System.out.println("- contentData="+aRestReq.getInputContentData());
		System.out.println("- UrlPath="+aRestReq.getUrlPath());
		System.out.println("- UrlPathParams="+aRestReq.getUrlPathParam());
		System.out.println("- echoAttrs="+aRestReq.getEchoJsonAttrs());
		//
		System.out.println("output:");
		System.out.println("- httpStatus="+aHttpResp.getHttp_status());
		System.out.println("- httpStatusMsg="+aHttpResp.getHttp_status_message());
		System.out.println("- contentType="+aHttpResp.getContent_type());
		System.out.println("- contentData="+aHttpResp.getContent_data());
		System.out.println();
		return aHttpResp;

	}
	
	@Override
	public HttpResp handleException(RESTServiceReq aRestReq, HttpResp aHttpResp,RESTApiException aException) {
		System.out.println();
		System.out.println("[ handleException ]");
		System.out.println("httpStatus="+aHttpResp.getHttp_status());
		System.out.println("httpStatusMsg="+aHttpResp.getHttp_status_message());
		System.out.println("contentType="+aHttpResp.getContent_type());
		System.out.println("contentData="+aHttpResp.getContent_data());
		
		if(aException!=null)
		{
			System.out.println("Exception="+aException.getMessage());
			
			Throwable t = aException.getCause();
			if(t!=null)
			{
				System.out.println("Cause="+t.getMessage());
			}
			
			StackTraceElement[] stackTraces = aException.getStackTrace();
			if(stackTraces!=null)
			{
				StringBuffer sb = new StringBuffer();
				for(int i=0; i<10; i++)
				{
					StackTraceElement e = stackTraces[i];
					sb.append(" ").append(e.getClassName()).append(":").append(e.getLineNumber()).append("\n");
				}
				System.out.println("StackTrace="+sb.toString());
			}
		}
		else
		{
			System.out.println("Exception="+aException);
		}
		
		System.out.println();
		return aHttpResp;
	}

}