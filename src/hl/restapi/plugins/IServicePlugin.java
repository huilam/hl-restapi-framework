package hl.restapi.plugins;

import hl.common.http.HttpResp;
import hl.restapi.service.RESTApiException;
import hl.restapi.service.RESTServiceReq;

public interface IServicePlugin {
	
	public HttpResp postProcess(RESTServiceReq aRestReq, HttpResp aHttpResp) throws RESTApiException;
	
	public HttpResp handleException(RESTServiceReq aRestReq, HttpResp aHttpResp, RESTApiException aException);
	
}