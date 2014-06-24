package rop.impl;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.format.support.FormattingConversionService;
import org.springframework.format.support.FormattingConversionServiceFactoryBean;
import org.springframework.util.Assert;
import org.springframework.validation.SmartValidator;
import rop.*;
import rop.converter.ConverterContainer;
import rop.error.MainErrors;
import rop.error.SubErrors;
import rop.event.*;
import rop.marshaller.FastjsonRopMarshaller;
import rop.marshaller.XStreamXmlRopMarshaller;
import rop.request.SystemParameterNames;
import rop.request.UploadFileConverter;
import rop.response.*;
import rop.security.*;
import rop.security.SecurityManager;
import rop.session.DefaultSessionManager;
import rop.session.SessionBindInterceptor;
import rop.session.SessionManager;
import rop.utils.RopUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

/**
 * <pre>
 *     服务路由器，处于服务路由核心关键，决定服务方法的调用，异常处理等
 * </pre>
 *
 * @author 陈雄华
 * @author luopeng
 * @version 1.0
 */
public class AnnotationServletServiceRouter implements ServiceRouter {

	public static final String APPLICATION_XML = "application/xml";

	public static final String APPLICATION_JSON = "application/json";
	public static final String ACCESS_CONTROL_ALLOW_ORIGIN = "Access-Control-Allow-Origin";
	public static final String ACCESS_CONTROL_ALLOW_METHODS = "Access-Control-Allow-Methods";
	public static final String DEFAULT_EXT_ERROR_BASE_NAME = "i18n/rop/ropError";

	protected final Logger logger = LoggerFactory.getLogger(getClass());

	private static final String I18N_ROP_ERROR = "i18n/rop/error";

	private ServiceMethodAdapter serviceMethodAdapter = new AnnotationServiceMethodAdapter();

	private RopMarshaller xmlMarshallerRop = new XStreamXmlRopMarshaller();

	private RopMarshaller jsonMarshallerRop = new FastjsonRopMarshaller();

	private RequestContextBuilder requestContextBuilder;

	private SecurityManager securityManager;

	private ConverterContainer converterContainer = new ConverterContainer();

	/**
	 * 服务主线程池，主要处理服务调用
	 */
	private ThreadPoolExecutor threadPoolExecutor;

	/**
	 * 事件广播线程池，主要处理事件广播
	 */
	private ThreadPoolExecutor eventPoolExecutor;

	private RopContext ropContext;

	private RopEventMulticaster ropEventMulticaster;

	private List<Interceptor> interceptors = new ArrayList<Interceptor>();

	private List<RopEventListener> listeners = new ArrayList<RopEventListener>();

	private boolean signEnable = true;

	private ApplicationContext applicationContext;

	//所有服务方法的最大过期时间，单位为秒(0或负数代表不限制)
	private int serviceTimeoutSeconds = Integer.MAX_VALUE;

	//会话管理器
	private SessionManager sessionManager = new DefaultSessionManager();

	//服务调用频率管理器
	private InvokeTimesController invokeTimesController = new DefaultInvokeTimesController();

	private String extErrorBasename;

	private String[] extErrorBasenames;

	//时间戳误差容忍
	private long timestampTolerance;

	/**
	 * ROP请求ID，在开始请求的时候就进行设置，贯穿整个请求周期
	 */
	public static final String ROP_REQUEST_ID = "_ROP_REQUEST_ID_";

	@Override
	public void service(Object request, Object response) {
		HttpServletRequest servletRequest = (HttpServletRequest) request;
		HttpServletResponse servletResponse = (HttpServletResponse) response;

		buildRopRequestId(servletRequest);

		//获取服务方法最大过期时间
		String method = ServletRequestContextBuilder.getMethod(servletRequest);
		String version = ServletRequestContextBuilder.getVersion(servletRequest);
		if (logger.isDebugEnabled()) {
			logger.debug("调用服务方法：" + method + "(" + version + ")");
		}
		int serviceMethodTimeout = getServiceMethodTimeout(method, version);
		long beginTime = System.currentTimeMillis();
		String jsonpCallback = ServletRequestContextBuilder.getJsonpCallback(servletRequest);

		//使用异常方式调用服务方法
		try {


		    //这里去掉了源代码中的"线程摆渡"代码，这里采用alibaba开源的multithread.context实现更为高雅 :)

			ServiceRunnable runnable = new ServiceRunnable(servletRequest);
			Future<RopResponse> future = this.threadPoolExecutor.submit(runnable);
			RopResponse ropResponse = future.get(serviceMethodTimeout, TimeUnit.SECONDS);

			writeResponse(servletRequest, servletResponse, ropResponse, ServletRequestContextBuilder.getResponseFormat(servletRequest), jsonpCallback);

		} catch (RejectedExecutionException ree) {//超过最大的服务平台的最大资源限制，无法提供服务
			if (logger.isInfoEnabled()) {
				logger.info("调用服务方法:" + method + "(" + version + ")，超过最大资源限制，无法提供服务。");
			}

			RejectedServiceResponse ropResponse = new RejectedServiceResponse(ServletRequestContextBuilder.getLocale(servletRequest));
			writeResponse(servletRequest, servletResponse, ropResponse, ServletRequestContextBuilder.getResponseFormat(servletRequest), jsonpCallback);

			fireErrorEvent(servletRequest, beginTime, ropResponse);
		} catch (TimeoutException e) {//服务时间超限
			if (logger.isInfoEnabled()) {
				logger.info("调用服务方法:" + method + "(" + version + ")，服务调用超时。");
			}

			ServiceTimeoutErrorResponse ropResponse = new ServiceTimeoutErrorResponse(method, ServletRequestContextBuilder.getLocale(servletRequest), serviceMethodTimeout);
			writeResponse(servletRequest, servletResponse, ropResponse, ServletRequestContextBuilder.getResponseFormat(servletRequest), jsonpCallback);

			fireErrorEvent(servletRequest, beginTime, ropResponse);

		} catch (Throwable throwable) {//产生未知的错误
			if (logger.isInfoEnabled()) {
				logger.info("调用服务方法:" + method + "(" + version + ")，产生异常", throwable);
			}
			ServiceUnavailableErrorResponse ropResponse = new ServiceUnavailableErrorResponse(method, ServletRequestContextBuilder.getLocale(servletRequest), throwable);
			writeResponse(servletRequest, servletResponse, ropResponse, ServletRequestContextBuilder.getResponseFormat(servletRequest), jsonpCallback);

			fireErrorEvent(servletRequest, beginTime, ropResponse);
		}
	}

	private void buildRopRequestId(HttpServletRequest servletRequest) {
		servletRequest.setAttribute(ROP_REQUEST_ID,UUID.randomUUID().toString());
	}

	private void fireErrorEvent(HttpServletRequest servletRequest, long beginTime, FailedRopResponse ropResponse) {
		RopRequestContext requestContext = buildRequestContext(servletRequest,beginTime);
		requestContext.setRopResponse(ropResponse);
		requestContext.setServiceEndTime(System.currentTimeMillis());
		if(ropResponse instanceof RejectedServiceResponse){
			fireServiceRejectedEvent(servletRequest, (RejectedServiceResponse)ropResponse,requestContext);
		}else if(ropResponse instanceof ServiceTimeoutErrorResponse){
			fireServiceTimeoutEvent(servletRequest,(ServiceTimeoutErrorResponse)ropResponse,requestContext);
		}else if(ropResponse instanceof ServiceUnavailableErrorResponse){
			fireServiceUnavailableEvent(servletRequest,(ServiceUnavailableErrorResponse)ropResponse,requestContext);
		}else {
			throw new RuntimeException("not supported FailedRopResponse type!");
		}
	}

	@Override
	public void startup() {
		if (logger.isInfoEnabled()) {
			logger.info("开始启动Rop框架...");
		}
		Assert.notNull(this.applicationContext, "Spring上下文不能为空");

		//初始化类型转换器
		registerConverters();

		//实例化ServletRequestContextBuilder
		this.requestContextBuilder = new ServletRequestContextBuilder(this.converterContainer, this.sessionManager);
		try {
			SmartValidator validator = applicationContext.getBean(SmartValidator.class);
			this.requestContextBuilder.setValidator(validator);
		} catch (BeansException e) {
			throw new RopException("can not found any validator.");
		}

		//设置校验器
		if (this.securityManager == null) {
			this.securityManager = new DefaultSecurityManager();
		}

		//设置异步执行器
		if (this.threadPoolExecutor == null) {
			ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("roptask-%d").build();
			this.threadPoolExecutor = new ThreadPoolExecutor(1, Integer.MAX_VALUE, 5 * 60, TimeUnit.SECONDS,
					new LinkedBlockingDeque<Runnable>(),threadFactory);
		}

		//如果不设置，则和异步执行器使用相同
		if(this.eventPoolExecutor == null){
			this.eventPoolExecutor = threadPoolExecutor;
		}

		//创建Rop上下文
		this.ropContext = buildRopContext();

		//初始化事件发布器
		this.ropEventMulticaster = buildRopEventMulticaster();

		//注册会话绑定拦截器
		this.addInterceptor(new SessionBindInterceptor());

		//初始化信息源
		initMessageSource();

		//产生Rop框架初始化事件
		fireRopStartedEvent();

		if (logger.isInfoEnabled()) {
			logger.info("Rop框架启动成功！");
		}
	}

	private void registerConverters() {
		converterContainer.addConverter(new UploadFileConverter());
	}

	@Override
	public void shutdown() {
		threadPoolExecutor.shutdown();
		fireRopClosedEvent();
	}

	@Override
	public void setSignEnable(boolean signEnable) {
		if (!signEnable && logger.isInfoEnabled()) {
			logger.info("关闭签名验证功能");
		}
		this.signEnable = signEnable;
	}

	@Override
	public void setTimestampTolerance(long timestampTolerance) {
		this.timestampTolerance = timestampTolerance;
	}

	@Override
	public void setInvokeTimesController(InvokeTimesController invokeTimesController) {
		this.invokeTimesController = invokeTimesController;
	}

	@Override
	public void setServiceTimeoutSeconds(int serviceTimeoutSeconds) {
		this.serviceTimeoutSeconds = serviceTimeoutSeconds;
	}

	@Override
	public void setSecurityManager(SecurityManager securityManager) {
		this.securityManager = securityManager;
	}

	@Override
	public void setSessionManager(SessionManager sessionManager) {
		this.sessionManager = sessionManager;
	}

	/**
	 * 获取默认的格式化转换器
	 *
	 * @return
	 */
	private FormattingConversionService getDefaultConversionService() {
		FormattingConversionServiceFactoryBean serviceFactoryBean = new FormattingConversionServiceFactoryBean();
		serviceFactoryBean.afterPropertiesSet();
		return serviceFactoryBean.getObject();
	}

	@Override
	public void setExtErrorBasename(String extErrorBasename) {
		this.extErrorBasename = extErrorBasename;
	}

	@Override
	public void setExtErrorBasenames(String[] extErrorBasenames) {
		if (extErrorBasenames != null) {
			List<String> list = new ArrayList<String>();
			for (String errorBasename : extErrorBasenames) {
				if (StringUtils.isNotBlank(errorBasename)) {
					list.add(errorBasename);
				}
			}
			this.extErrorBasenames = list.toArray(new String[0]);
		}
	}

	@Override
	public void setThreadPoolExecutor(ThreadPoolExecutor threadPoolExecutor) {
		this.threadPoolExecutor = threadPoolExecutor;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	@Override
	public RopContext getRopContext() {
		return this.ropContext;
	}

	@Override
	public void addInterceptor(Interceptor interceptor) {
		this.interceptors.add(interceptor);
	}

	@Override
	public void addListener(RopEventListener listener) {
		this.listeners.add(listener);
	}

	public int getServiceTimeoutSeconds() {
		return serviceTimeoutSeconds >= 0 ? serviceTimeoutSeconds : Integer.MAX_VALUE;
	}

	/**
	 * 取最小的过期时间
	 *
	 * @param method
	 * @param version
	 * @return
	 */
	private int getServiceMethodTimeout(String method, String version) {
		ServiceMethodHandler serviceMethodHandler = ropContext.getServiceMethodHandler(method, version);
		if (serviceMethodHandler == null) {
			return getServiceTimeoutSeconds();
		} else {
			int methodTimeout = serviceMethodHandler.getServiceMethodDefinition().getTimeout();
			if (methodTimeout <= 0) {
				return getServiceTimeoutSeconds();
			} else {
				return methodTimeout;
			}
		}
	}

	/**
	 *  此处去掉了"线程摆渡"代码，改由alibaba multithread.context 提供支持
	 *  modify by luopeng 2014-06-24
	 *
	 */
	private class ServiceRunnable implements Callable<RopResponse> {

		private HttpServletRequest servletRequest;

		private ServiceRunnable(HttpServletRequest servletRequest) {
			this.servletRequest = servletRequest;
		}

		@Override
		public RopResponse call() {

			RopRequestContext ropRequestContext = null;

			try {

				//第一阶段绑定
				ropRequestContext = requestContextBuilder.buildSystemParams(ropContext, servletRequest);

				//验证系统级参数的合法性
				MainError mainError = securityManager.validateSystemParameters(ropRequestContext);
				if (mainError != null) {
					ropRequestContext.setRopResponse(ErrorResponseUtils.buildErrorResponse(mainError));
				} else {

					//绑定业务数据（第二阶段绑定）
					requestContextBuilder.bindBusinessParams(ropRequestContext);

					//进行其它检查业务数据合法性，业务安全等
					mainError = securityManager.validateOther(ropRequestContext);
					if (mainError != null) {
						ropRequestContext.setRopResponse(ErrorResponseUtils.buildErrorResponse(mainError));
					} else {
						fireServiceStartedEvent(ropRequestContext);

						//服务处理前拦截
						invokeBeforceServiceOfInterceptors(ropRequestContext);

						if (ropRequestContext.getRopResponse() == null) { //拦截器未生成response
							//如果拦截器没有产生ropResponse时才调用服务方法
							ropRequestContext.setRopResponse(doService(ropRequestContext));
						}
					}
				}
				//by luopeng 不在此处输出，因为可能超时会引起异常输出同时进行
				return ropRequestContext.getRopResponse();
			} catch (Throwable e) {
				logger.error("ServiceRunnable Error:", e);
				String method = ropRequestContext.getMethod();
				Locale locale = ropRequestContext.getLocale();
				ServiceUnavailableErrorResponse ropResponse = new ServiceUnavailableErrorResponse(method, locale, e);
				ropRequestContext.setRopResponse(ropResponse);
				return ropResponse;
			} finally {
				if (ropRequestContext != null) {
					//发布服务完成事件
					ropRequestContext.setServiceEndTime(System.currentTimeMillis());
					//完成一次服务请求，计算次数
					invokeTimesController.caculateInvokeTimes(ropRequestContext.getAppKey(), ropRequestContext.getSession());
					//输出响应前拦截
					invokeBeforceResponseOfInterceptors(ropRequestContext);
					fireServiceFinishedEvent(ropRequestContext);
				}
			}
		}
	}


	/**
	 * 创建一个请求上下文对象
	 *
	 * @param request
	 * @param beginTime
	 * @return
	 */
	private RopRequestContext buildRequestContext(HttpServletRequest request, long beginTime) {
		RopRequestContext ropRequestContext = requestContextBuilder.buildSystemParams(ropContext, request);
		ropRequestContext.setServiceBeginTime(beginTime);
		ropRequestContext.setRequestId(ServletRequestContextBuilder.getRopRequestId(request));
		return ropRequestContext;
	}

	private RopContext buildRopContext() {
		DefaultRopContext defaultRopContext = new DefaultRopContext(this.applicationContext);
		defaultRopContext.setSignEnable(this.signEnable);
		defaultRopContext.setSessionManager(sessionManager);
		defaultRopContext.setTimestampTolerance(timestampTolerance);
		return defaultRopContext;
	}

	private RopEventMulticaster buildRopEventMulticaster() {

		SimpleRopEventMulticaster simpleRopEventMulticaster = new SimpleRopEventMulticaster();

		//设置异步执行器
		if (this.eventPoolExecutor != null) {
			simpleRopEventMulticaster.setExecutor(this.eventPoolExecutor);
		}

		//添加事件监听器
		if (this.listeners != null && this.listeners.size() > 0) {
			for (RopEventListener ropEventListener : this.listeners) {
				simpleRopEventMulticaster.addRopListener(ropEventListener);
			}
		}

		return simpleRopEventMulticaster;
	}

	/**
	 * 发布Rop启动后事件
	 */
	private void fireRopStartedEvent() {
		RopStartedEvent ropEvent = new RopStartedEvent(this, this.ropContext);
		this.ropEventMulticaster.multicastEvent(ropEvent);
	}

	/**
	 * 发布Rop停止事件
	 */
	private void fireRopClosedEvent() {
		RopClosedEvent ropEvent = new RopClosedEvent(this, this.ropContext);
		this.ropEventMulticaster.multicastEvent(ropEvent);
	}

	/**
	 * 发布服务完成事件
	 * @param ropRequestContext
	 */
	private void fireServiceFinishedEvent(RopRequestContext ropRequestContext) {
		this.ropEventMulticaster.multicastEvent(new ServiceFinishedEvent(this, ropRequestContext));
	}

	/**
	 * 发布服务开始事件
	 * @param ropRequestContext
	 */
	private void fireServiceStartedEvent(RopRequestContext ropRequestContext) {
		this.ropEventMulticaster.multicastEvent(new ServiceStartedEvent(this, ropRequestContext));
	}

	/**
	 * 发布服务被拒绝事件
	 * @param servletRequest
	 * @param ropResponse
	 */
	private void fireServiceRejectedEvent(HttpServletRequest servletRequest, RejectedServiceResponse ropResponse,RopRequestContext ropRequestContext) {
		this.ropEventMulticaster.multicastEvent(new ServiceRejectedEvent(servletRequest,ropResponse,ropRequestContext));

	}

	/**
	 * 发布服务超时事件
	 * @param servletRequest
	 * @param ropResponse
	 */
	private void fireServiceTimeoutEvent(HttpServletRequest servletRequest, ServiceTimeoutErrorResponse ropResponse,RopRequestContext ropRequestContext) {
		this.ropEventMulticaster.multicastEvent(new ServiceTimeoutEvent(servletRequest,ropResponse,ropRequestContext));

	}

	/**
	 * 发布服务未知错误事件
	 * @param servletRequest
	 * @param ropResponse
	 */
	private void fireServiceUnavailableEvent(HttpServletRequest servletRequest, ServiceUnavailableErrorResponse ropResponse,RopRequestContext ropRequestContext) {
		this.ropEventMulticaster.multicastEvent(new ServiceUnavailableEvent(servletRequest,ropResponse,ropRequestContext));
	}

	/**
	 * 发布业务调用错误事件
	 * @param businessExceptionEvent
	 */
	private void fireBusinessExceptionEvent(BusinessExceptionEvent businessExceptionEvent) {
		this.ropEventMulticaster.multicastEvent(businessExceptionEvent);
	}


	/**
	 * 在服务调用之前拦截
	 *
	 * @param ropRequestContext
	 */
	private void invokeBeforceServiceOfInterceptors(RopRequestContext ropRequestContext) {
		Interceptor tempInterceptor = null;
		try {
			if (interceptors != null && interceptors.size() > 0) {
				for (Interceptor interceptor : interceptors) {

					interceptor.beforeService(ropRequestContext);

					//如果有一个产生了响应，则阻止后续的调用
					if (ropRequestContext.getRopResponse() != null) {
						if (logger.isDebugEnabled()) {
							logger.debug("拦截器[" + interceptor.getClass().getName() + "]产生了一个RopResponse," +
										 " 阻止本次服务请求继续，服务将直接返回。");
						}
						return;
					}
				}
			}
		} catch (Throwable e) {
			ropRequestContext.setRopResponse(ErrorResponseUtils.buildServiceUnavailableErrorResponse(ropRequestContext.getMethod(), ropRequestContext.getLocale(), e));
			String interceptName = tempInterceptor == null ? "" : tempInterceptor.getClass().getName();
			logger.error("在执行拦截器[" + interceptName + "]时发生异常.", e);
		}
	}

	/**
	 * 在服务调用之后，返回响应之前拦截
	 *
	 * @param ropRequestContext
	 */
	private void invokeBeforceResponseOfInterceptors(RopRequestContext ropRequestContext) {

		if(ropRequestContext == null){
			logger.error("rop request context is null, maybe some errors occur before.");
			return ;
		}

		Interceptor tempInterceptor = null;
		try {
			if (interceptors != null && interceptors.size() > 0) {
				for (Interceptor interceptor : interceptors) {
					interceptor.beforeResponse(ropRequestContext);
				}
			}
		} catch (Throwable e) {
			ropRequestContext.setRopResponse(ErrorResponseUtils.buildServiceUnavailableErrorResponse(ropRequestContext.getMethod(), ropRequestContext.getLocale(), e));
			String interceptName = tempInterceptor == null ? "" : tempInterceptor.getClass().getName();
			logger.error("在执行拦截器[" + interceptName + "]时发生异常.", e);
		}
	}

	private void writeResponse(HttpServletRequest request, HttpServletResponse httpServletResponse, RopResponse ropResponse, MessageFormat messageFormat, String jsonpCallback) {
		try {

			RopMarshaller ropMarshaller = null;
			String contentType = null;
			if (messageFormat == MessageFormat.xml) {
				ropMarshaller = xmlMarshallerRop;
				contentType = APPLICATION_XML;
			} else {
				ropMarshaller = jsonMarshallerRop;
				contentType = APPLICATION_JSON;
			}

			String outContent = null;
			try {
				outContent = ropMarshaller.marshaller(ropResponse);
				if (logger.isDebugEnabled()) {
					logger.debug("RopResponse：" + outContent);
				}


			} catch (Exception e) {
				//防止序列化出错
				String method = request.getParameter(SystemParameterNames.getMethod());
				String localeStr = request.getParameter(SystemParameterNames.getLocale());
				Locale locale = RopUtils.getLocale(localeStr);
				outContent = ropMarshaller.marshaller(new ServiceUnavailableErrorResponse(method, locale, e));
				logger.error("Serialization Error.", e);
			}

			httpServletResponse.addHeader(ACCESS_CONTROL_ALLOW_ORIGIN, "*");
			httpServletResponse.addHeader(ACCESS_CONTROL_ALLOW_METHODS, "*");
			httpServletResponse.setCharacterEncoding(Constants.UTF8);

			httpServletResponse.setContentType(contentType);

			if (jsonpCallback != null) {
				outContent += jsonpCallback;
				outContent += "(";
				outContent += outContent;
				outContent += ");";
			}

			httpServletResponse.getOutputStream().write(outContent.getBytes(Constants.UTF8));

		} catch (IOException e) {
			throw new RopException(e);
		} finally {
			try {
				httpServletResponse.getOutputStream().flush();
				httpServletResponse.getOutputStream().close();
			} catch (IOException e) {
				logger.error("Close Response OutputStream Error", e);
			}
		}
	}

	private RopResponse doService(RopRequestContext ropRequestContext) {
		RopResponse ropResponse = null;
		if (ropRequestContext.getMethod() == null) {
			ropResponse = ErrorResponseUtils.buildErrorResponse(MainErrors.getError(MainErrorType.MISSING_METHOD, ropRequestContext.getLocale()));
		} else if (!ropContext.isValidMethod(ropRequestContext.getMethod())) {
			ropResponse = ErrorResponseUtils.buildErrorResponse(MainErrors.getError(MainErrorType.INVALID_METHOD, ropRequestContext.getLocale()));
		} else {
			try {
				ropResponse = serviceMethodAdapter.invokeServiceMethod(ropRequestContext);
			} catch (Exception e) { //出错则抛出服务不可用的异常
				if (logger.isInfoEnabled()) {
					logger.info("调用" + ropRequestContext.getMethod() + "时发生异常，异常信息为：" + e.getMessage());
				}
				ropResponse = ErrorResponseUtils.buildServiceUnavailableErrorResponse(ropRequestContext.getMethod(), ropRequestContext.getLocale(), e);
				HttpServletRequest request = (HttpServletRequest) ropRequestContext.getRawRequestObject();
                fireBusinessExceptionEvent(new BusinessExceptionEvent(request,ropResponse,ropRequestContext));
			}
		}
		return ropResponse;
	}

	/**
	 * 设置国际化资源信息
	 */
	private void initMessageSource() {
		HashSet<String> baseNamesSet = new HashSet<String>();
		baseNamesSet.add(I18N_ROP_ERROR);//ROP自动的资源

		if (extErrorBasename == null && extErrorBasenames == null) {
			baseNamesSet.add(DEFAULT_EXT_ERROR_BASE_NAME);
		} else {
			if (extErrorBasename != null) {
				baseNamesSet.add(extErrorBasename);
			}
			if (extErrorBasenames != null) {
				baseNamesSet.addAll(Arrays.asList(extErrorBasenames));
			}
		}
		String[] totalBaseNames = baseNamesSet.toArray(new String[0]);

		if (logger.isInfoEnabled()) {
			logger.info("加载错误码国际化资源：{}", StringUtils.join(totalBaseNames, ","));
		}
		ResourceBundleMessageSource bundleMessageSource = new ResourceBundleMessageSource();
		bundleMessageSource.setBasenames(totalBaseNames);
		MessageSourceAccessor messageSourceAccessor = new MessageSourceAccessor(bundleMessageSource);
		MainErrors.setErrorMessageSourceAccessor(messageSourceAccessor);
		SubErrors.setErrorMessageSourceAccessor(messageSourceAccessor);
	}

	public SecurityManager getSecurityManager() {
		return securityManager;
	}

	public ThreadPoolExecutor getThreadPoolExecutor() {
		return threadPoolExecutor;
	}

	public RopEventMulticaster getRopEventMulticaster() {
		return ropEventMulticaster;
	}

	public List<Interceptor> getInterceptors() {
		return interceptors;
	}

	public List<RopEventListener> getListeners() {
		return listeners;
	}

	public boolean isSignEnable() {
		return signEnable;
	}

	public ApplicationContext getApplicationContext() {
		return applicationContext;
	}

	public String getExtErrorBasename() {
		return extErrorBasename;
	}

	public long getTimestampTolerance() {
		return timestampTolerance;
	}

	public ThreadPoolExecutor getEventPoolExecutor() {
		return eventPoolExecutor;
	}

	public void setEventPoolExecutor(ThreadPoolExecutor eventPoolExecutor) {
		this.eventPoolExecutor = eventPoolExecutor;
	}
}