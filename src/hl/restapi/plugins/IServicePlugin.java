package hl.restapi.plugins;

import hl.common.http.HttpResp;
import hl.restapi.service.RESTApiException;
import hl.restapi.service.RESTServiceReq;

public interface IServicePlugin {
	
	public HttpResp postProcess(RESTServiceReq aRestReq, HttpResp aHttpResp);
	
	public HttpResp handleException(RESTServiceReq aRestReq, HttpResp aHttpResp, RESTApiException aException);
	
}