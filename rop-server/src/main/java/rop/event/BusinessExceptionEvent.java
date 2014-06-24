package rop.event;

import rop.RopRequestContext;
import rop.response.RopResponse;

import javax.servlet.http.HttpServletRequest;

/**
 *  业务异常事件
 * @author luopeng
 *         Created on 2014/6/23.
 */
public class BusinessExceptionEvent extends RopEvent {

	private RopResponse ropResponse;
	private HttpServletRequest httpServletRequest;
	private RopRequestContext ropRequestContext;

	public BusinessExceptionEvent(HttpServletRequest httpServletRequest,RopResponse response,RopRequestContext ropRequestContext) {
		super(response, ropRequestContext.getRopContext());
		this.ropResponse = response;
		this.httpServletRequest = httpServletRequest;
		this.ropRequestContext = ropRequestContext;
	}

	public RopResponse getRopResponse() {
		return ropResponse;
	}

	public void setRopResponse(RopResponse ropResponse) {
		this.ropResponse = ropResponse;
	}

	public HttpServletRequest getHttpServletRequest() {
		return httpServletRequest;
	}

	public void setHttpServletRequest(HttpServletRequest httpServletRequest) {
		this.httpServletRequest = httpServletRequest;
	}

	public RopRequestContext getRopRequestContext() {
		return ropRequestContext;
	}

	public void setRopRequestContext(RopRequestContext ropRequestContext) {
		this.ropRequestContext = ropRequestContext;
	}
}
