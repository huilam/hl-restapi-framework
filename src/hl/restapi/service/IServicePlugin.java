package hl.restapi.service;

import hl.common.http.HttpResp;

public interface IServicePlugin {
	 
	public RESTServiceReq preProcess(RESTServiceReq aRestReq);
	
	public HttpResp postProcess(RESTServiceReq aRestReq, HttpResp aHttpResp);
	
	public HttpResp handleException(RESTServiceReq aRestReq, HttpResp aHttpResp, RESTApiException aException);
	
}