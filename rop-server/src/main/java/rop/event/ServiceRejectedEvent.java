package rop.event;

import rop.RopRequestContext;
import rop.response.RejectedServiceResponse;

import javax.servlet.http.HttpServletRequest;

/**
 * 服务拒绝事件
 * @author luopeng
 *         Created on 2014/6/21.
 */
public class ServiceRejectedEvent extends RopEvent {

	private RejectedServiceResponse rejectedServiceResponse;

	private HttpServletRequest httpServletRequest;

	private RopRequestContext ropRequestContext;

	public ServiceRejectedEvent(HttpServletRequest httpServletRequest,RejectedServiceResponse rejectedServiceResponse,
								RopRequestContext ropRequestContext) {
		super(rejectedServiceResponse, ropRequestContext.getRopContext());
		this.httpServletRequest = httpServletRequest;
		this.rejectedServiceResponse = rejectedServiceResponse;
		this.ropRequestContext = ropRequestContext;
	}

	public RejectedServiceResponse getRejectedServiceResponse() {
		return rejectedServiceResponse;
	}

	public HttpServletRequest getHttpServletRequest() {
		return httpServletRequest;
	}

	public RopRequestContext getRopRequestContext() {
		return ropRequestContext;
	}
}
