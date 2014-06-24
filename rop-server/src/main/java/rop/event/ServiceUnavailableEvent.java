package rop.event;

import rop.RopRequestContext;
import rop.response.ServiceUnavailableErrorResponse;

import javax.servlet.http.HttpServletRequest;

/**
 * 服务不可用事件
 * @author luopeng
 *         Created on 2014/6/21.
 */
public class ServiceUnavailableEvent extends RopEvent {

	private HttpServletRequest httpServletRequest;
	private ServiceUnavailableErrorResponse serviceUnavailableErrorResponse;
	private RopRequestContext ropRequestContext;

	public ServiceUnavailableEvent(HttpServletRequest servletRequest, ServiceUnavailableErrorResponse response, RopRequestContext ropRequestContext) {
		super(response, ropRequestContext.getRopContext());
		this.httpServletRequest = httpServletRequest;
		this.serviceUnavailableErrorResponse = serviceUnavailableErrorResponse;
		this.ropRequestContext = ropRequestContext;
	}

	public HttpServletRequest getHttpServletRequest() {
		return httpServletRequest;
	}

	public ServiceUnavailableErrorResponse getServiceUnavailableErrorResponse() {
		return serviceUnavailableErrorResponse;
	}

	public RopRequestContext getRopRequestContext() {
		return ropRequestContext;
	}
}
