package hl.restapi.plugins;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

import hl.common.http.HttpResp;
import hl.restapi.service.RESTApiException;
import hl.restapi.service.RESTServiceReq;

public class EchoPlugin implements IServicePlugin {

	@Override
	public HttpResp postProcess(RESTServiceReq aRestReq, HttpResp aHttpResp) {
		PrintStream prn = System.out;
		try {
			prn = new PrintStream(System.out, true, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			 prn = System.out;
		}
		
		//Copy
		aHttpResp.setContent_type(aRestReq.getInputContentType());
		aHttpResp.setContent_data(aRestReq.getInputContentData());
		aHttpResp.setHttp_status(200);
		//
		
		
		prn.println();
		prn.println("[ postProcess ]");
		prn.println("input:");
		prn.println("- httpMethod="+aRestReq.getHttpMethod());
		prn.println("- contentType="+aRestReq.getInputContentType());
		prn.println("- contentData="+aRestReq.getInputContentData());
		prn.println("- UrlPath="+aRestReq.getUrlPath());
		prn.println("- UrlPathParams="+aRestReq.getUrlPathParam());
		prn.println("- echoAttrs="+aRestReq.getEchoJsonAttrs());
		//
		prn.println("output:");
		prn.println("- httpStatus="+aHttpResp.getHttp_status());
		prn.println("- httpStatusMsg="+aHttpResp.getHttp_status_message());
		prn.println("- contentType="+aHttpResp.getContent_type());
		prn.println("- contentData="+aHttpResp.getContent_data());
		prn.println();
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