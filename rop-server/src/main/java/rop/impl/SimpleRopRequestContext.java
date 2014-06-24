package rop.impl;

import com.google.common.collect.Lists;
import org.apache.commons.fileupload.FileItem;
import rop.*;
import rop.annotation.HttpAction;
import rop.request.ServiceRequest;
import rop.request.SystemParameterNames;
import rop.response.RopResponse;
import rop.security.MainError;
import rop.session.Session;
import rop.utils.RopUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * <pre>
 *     简单请求上下文
 * </pre>
 *
 * @author 陈雄华
 * @author luopeng
 * @version 1.0
 */
public class SimpleRopRequestContext implements RopRequestContext {

	public static final String SPRING_VALIDATE_ERROR_ATTRNAME = "$SPRING_VALIDATE_ERROR_ATTRNAME";

	private RopContext ropContext;

	private String method;

	private String version;

	private String appKey;

	private String sessionId;

	private Locale locale;

	private String format;

	public static ThreadLocal<MessageFormat> messageFormat = new ThreadLocal<MessageFormat>();

	private String sign;

	private Map<String, Object> attributes = new HashMap<String, Object>();

	private ServiceMethodHandler serviceMethodHandler;

	private MainError mainError;

	private volatile RopResponse ropResponse;

	private ServiceRequest serviceRequest;

	private long serviceBeginTime = -1;

	private long serviceEndTime = -1;

	private String ip;

	private HttpAction httpAction;

	private Object rawRequestObject;

	private Map<String, String> requestBodyMap = new HashMap<String,String>();

	private Map<String,String> requestHeaderMap = new HashMap<String,String>();

	private String requestId;

	private Object appkeyUserId;

	/**
	 * 请求时间戳
	 */
	private long timestamp = 0;

	private Object[] serviceMethodParameters = new Object[] { };

	/**
	 * 文件上传Item
	 */
	private List<FileItem> fileItems = Lists.newLinkedList();

	@Override
	public long getServiceBeginTime() {
		return this.serviceBeginTime;
	}

	@Override
	public long getServiceEndTime() {
		return this.serviceEndTime;
	}

	@Override
	public void setServiceBeginTime(long serviceBeginTime) {
		this.serviceBeginTime = serviceBeginTime;
	}

	@Override
	public void setServiceEndTime(long serviceEndTime) {
		this.serviceEndTime = serviceEndTime;
	}

	@Override
	public String getFormat() {
		return this.format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	@Override
	public Object getRawRequestObject() {
		return this.rawRequestObject;
	}

	public void setRawRequestObject(Object rawRequestObject) {
		this.rawRequestObject = rawRequestObject;
	}

	public SimpleRopRequestContext(RopContext ropContext) {
		this.ropContext = ropContext;
		this.serviceBeginTime = System.currentTimeMillis();
	}


	@Override
	public String getIp() {
		return this.ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	@Override
	public RopContext getRopContext() {
		return ropContext;
	}

	@Override
	public String getMethod() {
		return this.method;
	}

	public void setMethod(String method) {
		this.method = method;
		this.requestHeaderMap.put(SystemParameterNames.getMethod(), method);
	}

	@Override
	public String getSessionId() {
		return this.sessionId;
	}

	@Override
	public Session getSession() {
		if (ropContext != null && ropContext.getSessionManager() != null && getSessionId() != null) {
			return ropContext.getSessionManager().getSession(getSessionId());
		} else {
			return null;
		}
	}

	@Override
	public void addSession(String sessionId, Session session) {
		if (ropContext != null && ropContext.getSessionManager() != null) {
			ropContext.getSessionManager().addSession(sessionId, session);
		}
	}

	@Override
	public void removeSession() {
		if (ropContext != null && ropContext.getSessionManager() != null) {
			ropContext.getSessionManager().removeSession(getSessionId());
		}
	}

	@Override
	public Locale getLocale() {
		return this.locale;
	}

	public ServiceMethodHandler getServiceMethodHandler() {
		return this.serviceMethodHandler;
	}

	@Override
	public MessageFormat getMessageFormat() {
		return messageFormat.get();
	}

	@Override
	public RopResponse getRopResponse() {
		return this.ropResponse;
	}

	@Override
	public Object[] getServiceMethodParameters() {
		return serviceMethodParameters;
	}

	@Override
	public void setServiceMethodParameters(Object[] parameters) {
		this.serviceMethodParameters = parameters;
	}

	public String getAppKey() {
		return this.appKey;
	}

	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	public void setServiceMethodHandler(ServiceMethodHandler serviceMethodHandler) {
		this.serviceMethodHandler = serviceMethodHandler;
	}

	public void setMessageFormat(MessageFormat messageFormat) {
		this.messageFormat.set(messageFormat);
	}

	@Override
	public void setRopResponse(RopResponse ropResponse) {
		this.ropResponse = ropResponse;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public void setAppKey(String appKey) {
		this.appKey = appKey;
	}

	@Override
	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	@Override
	public String getSign() {
		return sign;
	}

	public void setSign(String sign) {
		this.sign = sign;
	}

	public void setMainError(MainError mainError) {
		this.mainError = mainError;
	}

	public MainError getMainError() {
		return this.mainError;
	}

	@Override
	public Object getAttribute(String name) {
		return this.attributes.get(name);
	}

	@Override
	public void setAttribute(String name, Object value) {
		this.attributes.put(name, value);
	}

	public Map<String, Object> getAttributes() {
		return attributes;
	}

	public void setAttributes(Map<String, Object> attributes) {
		this.attributes = attributes;
	}

	@Override
	public boolean isSignEnable() {
		return ropContext.isSignEnable() && !getServiceMethodDefinition().isIgnoreSign();
	}

	@Override
	public ServiceMethodDefinition getServiceMethodDefinition() {
		return serviceMethodHandler.getServiceMethodDefinition();
	}

	@Override
	public Map<String, String> getRequestBodyMap() {
		return this.requestBodyMap;
	}

	@Override
	public Map<String, String> getRequestHeaderMap() {
		return requestHeaderMap;
	}

	public void setRequestHeaderMap(Map<String, String> headers){
		this.requestHeaderMap = headers;
	}

	public void setRequestBodyMap(Map<String, String> requestBodyMap) {
		this.requestBodyMap = requestBodyMap;
	}

	@Override
	public String getBodyParameter(String paramName) {
		if (requestBodyMap != null) {
			return requestBodyMap.get(paramName);
		} else {
			return null;
		}
	}

	public void setHttpAction(HttpAction httpAction) {
		this.httpAction = httpAction;
	}

	@Override
	public HttpAction getHttpAction() {
		return this.httpAction;
	}

	@Override
	public String getRequestId() {
		return this.requestId;
	}

	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}

	@Override
	public long getTimestamp() {
		return this.timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public List<FileItem> getFileItems() {
		return fileItems;
	}

	@Override
	public void setAppkeyUserId(Object appkeyUserId) {
		this.appkeyUserId = appkeyUserId;
	}

	@Override
	public Object getAppkeyUserId() {
		return appkeyUserId;
	}

	public void setFileItems(List<FileItem> fileItems) {
		this.fileItems = fileItems;
	}
}

