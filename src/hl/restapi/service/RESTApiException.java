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

public class RESTApiException extends Exception {

	private static final long serialVersionUID = -1068899117288657750L;
	private String error_code = null;
	private String error_msg = null;

	public RESTApiException(String aErrCode, String aErrMessage)
	{
		super(aErrMessage);
		error_code = aErrCode;
		error_msg = aErrMessage;
	}
	
	public RESTApiException(String aErrCode, String aErrMessage, Throwable aThrowable)
	{
		super(aErrMessage, aThrowable);
		error_code = aErrCode;
		error_msg = aErrMessage;
	}
	
	public RESTApiException(String aErrCode, Throwable aThrowable)
	{
		super(aThrowable);
		error_code = aErrCode;
		error_msg = aThrowable.getMessage();
	}
	
	public String getErrorCode()
	{
		return error_code;
	}
	
	public String getErrorMsg()
	{
		return error_msg;
	}
	
	public String getMessage()
	{
		return getErrorCode()+":"+getErrorMsg();
	}
}