package rop.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rop.RopUnmarshaller;
import rop.annotation.IgnoreSign;
import rop.annotation.Temporary;
import rop.converter.Complex;
import rop.converter.RopConverter;
import rop.converter.Style;
import rop.json.FastjsonRopUnmarshaller;
import rop.request.*;
import rop.thirdparty.com.alibaba.fastjson.JSON;
import rop.thirdparty.com.alibaba.fastjson.serializer.SerializerFeature;
import rop.thirdparty.org.apache.commons.lang3.StringUtils;
import rop.utils.RopUtils;
import rop.utils.spring.AnnotationUtils;
import rop.utils.spring.Assert;
import rop.utils.spring.ClassUtils;
import rop.utils.spring.ReflectionUtils;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.*;

/**
 * 默认RopClient实现
 *
 * @author 陈雄华
 * @author luopeng
 */
public class DefaultRopClient implements RopClient {

	protected final Logger logger = LoggerFactory.getLogger(getClass());

	private String serverUrl;

	private String appKey;

	private String appSecret;

	private String sessionId;

	private boolean needSign = true;

	private Locale locale = Locale.SIMPLIFIED_CHINESE;

	private Map<String, String> extInfoMap = new HashMap<String,String>(2);

	private RopUnmarshaller ropUnmarshaller = new FastjsonRopUnmarshaller();

	//请求类所有请求参数
	private Map<Class<?>, List<Field>> requestAllFields = new HashMap<Class<?>, List<Field>>();

	//请求类所有不需要进行签名的参数
	private Map<Class<?>, Set<String>> requestIgnoreSignFieldNames = new HashMap<Class<?>, Set<String>>();


	//键为转换的目标类型
	private static Map<Class<?>, RopConverter<?>> ropConverterMap = new HashMap<Class<?>, RopConverter<?>>();

	{
		UploadFileConverter uploadFileConverter = new UploadFileConverter();
		ropConverterMap.put(uploadFileConverter.getSupportClass(), uploadFileConverter);
	}

	public static class Builder {

		private DefaultRopClient ropClient;

		public Builder() {
			ropClient = new DefaultRopClient();
		}

		public Builder withAppkey(String appkey) {
			ropClient.appKey = appkey;
			return this;
		}

		public Builder withAppSecret(String appSecret) {
			ropClient.appSecret = appSecret;
			return this;
		}

		public Builder withServerUrl(String serverUrl) {
			ropClient.serverUrl = serverUrl;
			return this;
		}

		public Builder withLocale(Locale locale) {
			ropClient.locale = locale;
			return this;
		}

		public Builder withExtInfo(String extKey, String extValue) {
			ropClient.extInfoMap.put(extKey, extValue);
			return this;
		}

		public Builder withSignSwitch(boolean signSwitch) {
			ropClient.needSign = signSwitch;
			return this;
		}

		public DefaultRopClient build() {
			if (StringUtils.isBlank(ropClient.serverUrl)) {
				throw new RuntimeException("server url can not be null");
			} else if (StringUtils.isBlank(ropClient.appKey)) {
				throw new RuntimeException("appKey can not be null");
			} else if (ropClient.needSign && StringUtils.isBlank(ropClient.appSecret)) {
				throw new RuntimeException("appSecret can not be null");
			}
			return ropClient;
		}

	}

	public Locale getLocale() {
		return locale;
	}

	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	@Override
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	@Override
	public void setTimestampParamName(String timestampParamName) {
		SystemParameterNames.setTimestamp(timestampParamName);
	}

	@Override
	public Map<String, String> getExtInfoMap() {
		return this.extInfoMap;
	}

	@Override
	public RopClient setAppKeyParamName(String paramName) {
		SystemParameterNames.setAppKey(paramName);
		return this;
	}

	@Override
	public RopClient setSessionIdParamName(String paramName) {
		SystemParameterNames.setSessionId(paramName);
		return this;
	}

	@Override
	public RopClient setMethodParamName(String paramName) {
		SystemParameterNames.setMethod(paramName);
		return this;
	}

	@Override
	public RopClient setVersionParamName(String paramName) {
		SystemParameterNames.setVersion(paramName);
		return this;
	}

	@Override
	public RopClient setFormatParamName(String paramName) {
		SystemParameterNames.setFormat(paramName);
		return this;
	}

	@Override
	public RopClient setLocaleParamName(String paramName) {
		SystemParameterNames.setLocale(paramName);
		return this;
	}

	@Override
	public RopClient setSignParamName(String paramName) {
		SystemParameterNames.setSign(paramName);
		return this;
	}

	@Override
	public void addRopConvertor(RopConverter ropConverter) {
		this.ropConverterMap.put(ropConverter.getSupportClass(), ropConverter);
	}

	@Override
	public ClientRequest buildClientRequest() {
		return new DefaultClientRequest(this);
	}

	@Override
	public ClientRequest buildClientRequest(int connTimeout,int readTimeout) {
		return new DefaultClientRequest(this,connTimeout,readTimeout);
	}

	@Override
	public void destroy() {
		//do nothing
	}

	@Override
	public void enableSign(boolean needSign) {
		this.needSign = needSign;
	}

	private class DefaultClientRequest implements ClientRequest {

		private RopClient ropClient;

		private Map<String, String> headerParamMap = new HashMap<String, String>(20);

		private Map<String, String> extInfoMap = new HashMap<String, String>(2);

		private Map<String, String> bodyParamMap = new HashMap<String, String>(4);

		private Set<String> ignoreSignParams = new HashSet<>();

		private int connTimeout ;

		private int readTimeout ;

		private DefaultClientRequest(RopClient ropClient,int connTimeout,int readTimeout) {
			this.ropClient = ropClient;
			this.connTimeout = connTimeout;
			this.readTimeout = readTimeout;

			headerParamMap.put(SystemParameterNames.getAppKey(), appKey);
			headerParamMap.put(SystemParameterNames.getLocale(), locale.toString());
			headerParamMap.put(SystemParameterNames.getTimestamp(), String.valueOf(System.currentTimeMillis()));
			if (sessionId != null) {
				headerParamMap.put(SystemParameterNames.getSessionId(), sessionId);
			}

			extInfoMap = ropClient.getExtInfoMap();

		}

		private DefaultClientRequest(RopClient ropClient) {
			this(ropClient,60000,600000);
		}

		@Override
		public ClientRequest setJsonpCallback(String callback) {
			headerParamMap.put(SystemParameterNames.getJsonp(), callback);
			return this;
		}

		@Override
		public ClientRequest addHeader(String name, String value) {
			headerParamMap.put(name, value);
			return this;
		}

		@Override
		public ClientRequest addBodyParam(String paramName, Object paramValue) {
			addBodyParam(paramName, paramValue, false);
			return this;
		}

		@Override
		public ClientRequest clearAllParam() {
			headerParamMap.clear();
			bodyParamMap.clear();
			extInfoMap.clear();
			return this;
		}

		@Override
		public ClientRequest addBodyParam(String paramName, Object paramValue, boolean ignoreSign) {
			Assert.isTrue(paramName != null && paramName.length() > 0, "参数名不能为空");
			Assert.notNull(paramValue, "参数值不能为null");

			//将参数添加到参数列表中
			String valueAsStr = paramValue.toString();
			if (ropConverterMap.containsKey(paramValue.getClass())) {
				RopConverter ropConverter = ropConverterMap.get(paramValue.getClass());
				valueAsStr = ropConverter.convertToString(paramValue);
			}
			bodyParamMap.put(paramName, valueAsStr);

			IgnoreSign typeIgnore = AnnotationUtils.findAnnotation(paramValue.getClass(), IgnoreSign.class);
			if (ignoreSign || typeIgnore != null) {
				ignoreSignParams.add(paramName);
			}
			return this;
		}

		@Override
		public <T> T post(Class<T> objectType, String methodName, String version) {
			return post(null, objectType, methodName, version);
		}

		@Override
		public <T> T post(ServiceRequest serviceRequest, Class<T> objectType, String methodName, String version) {
			fillAndSignParamMap(serviceRequest, methodName, version);

			Map<String, String> headers = resolveHeaders();
			String responseStr = null;
			try {
				responseStr = HttpWorker.getInstance().post(serverUrl, headers, bodyParamMap, false, connTimeout, readTimeout);
			} catch (IOException e) {
				throw new RuntimeException("error occur during http request.", e);
			}
			if (logger.isDebugEnabled()) {
				logger.debug("response:\n" + responseStr);
			}
			return toRopResponse(responseStr, objectType);
		}

		private Map<String, String> resolveHeaders() {
			Map<String, String> headers = new HashMap<String, String>();
			if (headerParamMap.get(SystemParameterNames.getSign()) != null) {
				headers.put(SystemParameterNames.getSign(), headerParamMap.get(SystemParameterNames.getSign()));
			}
			headers.put(SystemParameterNames.getTimestamp(), headerParamMap.get(SystemParameterNames.getTimestamp()));
			headers.put(SystemParameterNames.getAppKey(), headerParamMap.get(SystemParameterNames.getAppKey()));
			headers.put(SystemParameterNames.getFormat(), headerParamMap.get(SystemParameterNames.getFormat()));
			if (headerParamMap.get(SystemParameterNames.getJsonp()) != null) {
				headers.put(SystemParameterNames.getJsonp(), headerParamMap.get(SystemParameterNames.getJsonp()));
			}
			if (headerParamMap.get(SystemParameterNames.getSessionId()) != null) {
				headers.put(SystemParameterNames.getSessionId(), headerParamMap.get(SystemParameterNames.getSessionId()));
			}

			headers.put(SystemParameterNames.getLocale(), headerParamMap.get(SystemParameterNames.getLocale()));
			headers.put(SystemParameterNames.getMethod(), headerParamMap.get(SystemParameterNames.getMethod()));
			headers.put(SystemParameterNames.getVersion(), headerParamMap.get(SystemParameterNames.getVersion()));

			//设置扩展信息
			if (extInfoMap != null && !extInfoMap.isEmpty()) {
				headers.put(SystemParameterNames.getExtInfo(), RopUtils.encryptExtInfo(extInfoMap));
			}

			return headers;
		}

		@Override
		public <T> T post(RichServiceRequest request, Class<T> objectType) {
			return post(request, objectType, request.getMethod(), request.getVersion());
		}

		@Override
		public <T> T post(RichServiceRequest request) {
			return (T) post(request, request.getResponseClass());
		}

		@Override
		public <T> T postWithMultipart(Class<T> objectType, String methodName, String version) {
			return postWithMultipart(null, objectType, methodName, version);
		}

		@Override
		public <T> T postWithMultipart(ServiceRequest serviceRequest, Class<T> objectType, String methodName, String version) {
			fillAndSignParamMap(serviceRequest, methodName, version);
			Map<String, String> headers = resolveHeaders();

			String responseStr = null;
			try {
				responseStr = HttpWorker.getInstance().post(serverUrl, headers, bodyParamMap, true, connTimeout, readTimeout);
			} catch (IOException e) {
				throw new RuntimeException("error occur during http request.", e);
			}

			if (logger.isDebugEnabled()) {
				logger.debug("response:\n" + responseStr);
			}
			return toRopResponse(responseStr, objectType);
		}

		@Override
		public <T> T postWithMultipart(RichServiceRequest request, Class<T> objectType) {
			return postWithMultipart(request, objectType, request.getMethod(), request.getVersion());
		}

		@Override
		public <T> T postWithMultipart(RichServiceRequest request) {
			return (T) postWithMultipart(request, request.getResponseClass());
		}


		@Override
		public <T> T get(Class<T> objectType, String methodName, String version) {
			return get(null, objectType, methodName, version);
		}

		@Override
		public <T> T get(ServiceRequest serviceRequest, Class<T> objectType, String methodName, String version) {
			fillAndSignParamMap(serviceRequest, methodName, version);
			Map<String, String> headers = resolveHeaders();

			String responseStr = null;
			try {
				responseStr = HttpWorker.getInstance().get(buildGetUrl(bodyParamMap), headers, connTimeout, readTimeout);
			} catch (IOException e) {
				throw new RuntimeException("error occur during http request.", e);
			}

			if (logger.isDebugEnabled()) {
				logger.debug("response:\n" + responseStr);
			}
			return toRopResponse(responseStr, objectType);
		}

		@Override
		public <T> T get(RichServiceRequest request, Class<T> objectType) {
			return get(request, objectType, request.getMethod(), request.getVersion());
		}

		@Override
		public <T> T get(RichServiceRequest request) {
			return (T) get(request, request.getResponseClass());
		}

		private <T> T toRopResponse(String content, Class<T> objectType) {
			if (logger.isDebugEnabled()) {
				logger.debug(content);
			}
			return ropUnmarshaller.unmarshaller(content, objectType);
		}

		private String buildGetUrl(Map<String, String> form) {
			StringBuilder requestUrl = new StringBuilder();
			requestUrl.append(serverUrl);
			requestUrl.append("?");
			String joinChar = "";
			for (Map.Entry<String, String> entry : form.entrySet()) {
				requestUrl.append(joinChar);
				requestUrl.append(entry.getKey());
				requestUrl.append("=");
				requestUrl.append(entry.getValue());
				joinChar = "&";
			}
			return requestUrl.toString();
		}

		private void fillAndSignParamMap(ServiceRequest serviceRequest, String methodName, String version) {

			//系统级参数
			headerParamMap.put(SystemParameterNames.getMethod(), methodName);
			headerParamMap.put(SystemParameterNames.getVersion(), version);

			//业务级参数
			if (serviceRequest != null) {
				bodyParamMap.putAll(getParamFields(serviceRequest));
			}

			//对请求进行签名
			if (needSign) {
				Class<? extends ServiceRequest> payloadClass = (serviceRequest == null ? null : serviceRequest.getClass());
				String signValue = sign(payloadClass, appSecret, bodyParamMap, headerParamMap, extInfoMap);
				headerParamMap.put(SystemParameterNames.getSign(), signValue);
			}
		}

		/**
		 * 对请求参数进行签名
		 *
		 * @param ropRequestClass
		 * @param appSecret
		 * @param form
		 * @return
		 */
		private String sign(Class<?> ropRequestClass, String appSecret, Map<String, String> form, Map<String, String> headerMap, Map<String, String> extInfo) {
			Set<String> ignoreFieldNames = requestIgnoreSignFieldNames.get(ropRequestClass);
			if (ignoreFieldNames != null) {
				ignoreSignParams.addAll(ignoreFieldNames);
			}
			return RopUtils.sign(form, ignoreSignParams, headerMap, extInfo, appSecret);
		}

		/**
		 * 获取ropRequest对应的参数名列表
		 *
		 * @param serviceRequest
		 * @param mf
		 * @return
		 */
		private Map<String, String> getParamFields(ServiceRequest serviceRequest) {
			if (!requestAllFields.containsKey(serviceRequest.getClass())) {
				parseRopRequestClass(serviceRequest);
			}
			return toParamValueMap(serviceRequest);
		}

		/**
		 * 获取ropRequest对象的对应的参数列表
		 *
		 * @param serviceRequest
		 * @param mf
		 * @return
		 */
		private Map<String, String> toParamValueMap(ServiceRequest serviceRequest) {
			List<Field> fields = requestAllFields.get(serviceRequest.getClass());
			Map<String, String> params = new HashMap<String, String>();
			for (Field field : fields) {
				RopConverter convertor = getConvertor(field.getType());
				Object fieldValue = ReflectionUtils.getField(field, serviceRequest);
				if (fieldValue != null) {
					if (convertor != null) {//有对应转换器
						String strParamValue = convertor.convertToString(fieldValue);
						params.put(field.getName(), strParamValue);
					} else {
						Complex complex = AnnotationUtils.getAnnotation(field, Complex.class);
						if (complex != null && Style.JSON.equals(complex.style())) {
							params.put(field.getName(), JSON.toJSONString(fieldValue, SerializerFeature.DisableCircularReferenceDetect));
						} else {
							params.put(field.getName(), fieldValue.toString());
						}
					}
				}
			}
			return params;
		}
	}

	private RopConverter getConvertor(Class<?> fieldType) {
		for (Class<?> aClass : ropConverterMap.keySet()) {
			if (ClassUtils.isAssignable(aClass, fieldType)) {
				return ropConverterMap.get(aClass);
			}
		}
		return null;
	}

	private void parseRopRequestClass(ServiceRequest serviceRequest) {
		final ArrayList<Field> allFields = new ArrayList<Field>();
		final Set<String> ignoreSignFieldNames = RequestUtils.getIgnoreSignFieldNames(serviceRequest.getClass());
		ReflectionUtils.doWithFields(serviceRequest.getClass(), new ReflectionUtils.FieldCallback() {
			@Override
			public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
				ReflectionUtils.makeAccessible(field);
				if (!isTemporaryField(field)) {
					allFields.add(field);
				}
			}

			private boolean isTemporaryField(Field field) {
				Annotation[] declaredAnnotations = field.getDeclaredAnnotations();
				if (declaredAnnotations != null) {
					for (Annotation declaredAnnotation : declaredAnnotations) {
						Temporary varTemporary = field.getAnnotation(Temporary.class);
						if (varTemporary != null) {
							return true;
						}
					}
				}
				return false;
			}
		});

		requestAllFields.put(serviceRequest.getClass(), allFields);
		requestIgnoreSignFieldNames.put(serviceRequest.getClass(), ignoreSignFieldNames);
	}

	/**
	 * 获取ropRequest对应的参数名列表
	 *
	 * @param serviceRequest
	 * @param mf
	 * @return
	 */
	private Map<String, String> getParamFields(ServiceRequest serviceRequest) {
		if (!requestAllFields.containsKey(serviceRequest.getClass())) {
			parseRopRequestClass(serviceRequest);
		}
		return toParamValueMap(serviceRequest);
	}

	/**
	 * 获取ropRequest对象的对应的参数列表
	 *
	 * @param serviceRequest
	 * @param mf
	 * @return
	 */
	private Map<String, String> toParamValueMap(ServiceRequest serviceRequest) {
		List<Field> fields = requestAllFields.get(serviceRequest.getClass());
		Map<String, String> params = new HashMap<String, String>();
		for (Field field : fields) {
			RopConverter convertor = getConvertor(field.getType());
			Object fieldValue = ReflectionUtils.getField(field, serviceRequest);
			if (fieldValue != null) {
				if (convertor != null) {//有对应转换器
					String strParamValue = convertor.convertToString(fieldValue);
					params.put(field.getName(), strParamValue);
				} else {
					params.put(field.getName(), fieldValue.toString());
				}
			}
		}
		return params;
	}


}

