package rop.event;

import rop.RopRequestContext;
import rop.response.ServiceTimeoutErrorResponse;

import javax.servlet.http.HttpServletRequest;

/**
 * 服务超时事件
 * @author luopeng
 *         Created on 2014/6/21.
 */
public class ServiceTimeoutEvent extends RopEvent {

	private ServiceTimeoutErrorResponse serviceTimeoutErrorResponse;
	private HttpServletRequest httpServletRequest;
	private RopRequestContext ropRequestContext;

	public ServiceTimeoutEvent(HttpServletRequest httpServletRequest, ServiceTimeoutErrorResponse response, RopRequestContext ropRequestContext) {
		super(response, ropRequestContext.getRopContext());
		this.httpServletRequest = httpServletRequest;
		this.serviceTimeoutErrorResponse = response;
		this.ropRequestContext = ropRequestContext;
	}

	public ServiceTimeoutErrorResponse getServiceTimeoutErrorResponse() {
		return serviceTimeoutErrorResponse;
	}

	public HttpServletRequest getHttpServletRequest() {
		return httpServletRequest;
	}

	public RopRequestContext getRopRequestContext() {
		return ropRequestContext;
	}
}
