package rop.client;

import rop.request.RichServiceRequest;
import rop.request.ServiceRequest;

import java.io.IOException;

/**
 * 客户端请求
 * @author 陈雄华
 * @author luopeng
 */
public interface ClientRequest {


	/**
	 * 设置JSONP回调
	 * @param callback
	 * @return
	 */
	ClientRequest setJsonpCallback(String callback);

	/**
	 * 添加请求系统Header参数
	 * @param name
	 * @param value
	 * @return
	 */
	ClientRequest addHeader(String name, String value);

    /**
     * 添加请求参数,默认需要签名，如果类已经标注了{@link rop.annotation.IgnoreSign}则始终不加入签名
     * @param paramName
     * @param paramValue
     * @return
     */
    ClientRequest addBodyParam(String paramName, Object paramValue);

    /**
     * 添加请求参数,如果ignoreSign=true表示不参于签名
     * @param paramName
     * @param paramValue
     * @param ignoreSign
     * @return
     */
    ClientRequest addBodyParam(String paramName, Object paramValue, boolean ignoreSign);

    /**
     * 清除参数列表
     * @return
     */
    ClientRequest clearAllParam();

    /**
     * 使用POST发起请求
     * @param objectType
     * @param methodName
     * @param version
     * @param <T>
     * @return
     */
    <T> T post(Class<T> objectType, String methodName, String version);

    /**
     * 直接使用 ropRequest发送请求
     * @param serviceRequest
     * @param objectType
     * @param methodName
     * @param version
     * @param <T>
     * @return
     */
    <T> T post(ServiceRequest serviceRequest, Class<T> objectType, String methodName, String version) throws IOException;

	/**
	 * 直接使用 ropRequest发送请求
	 * @param request
	 * @param objectType
	 * @param <T>
	 * @return
	 */
	<T> T post(RichServiceRequest request,Class<T> objectType);

	/**
	 * 直接使用 ropRequest发送请求
	 * @param request
	 * @param <T>
	 * @return
	 */
	<T> T post(RichServiceRequest request);

	/**
	 * 使用POST发起multipart请求
	 * @param objectType
	 * @param methodName
	 * @param version
	 * @param <T>
	 * @return
	 */
	<T> T postWithMultipart(Class<T> objectType, String methodName, String version);

	/**
	 * 使用POST发起multipart请求
	 * @param serviceRequest
	 * @param objectType
	 * @param methodName
	 * @param version
	 * @param <T>
	 * @return
	 */
	<T> T postWithMultipart(ServiceRequest serviceRequest, Class<T> objectType, String methodName, String version);

	/**
	 * 使用POST发起multipart请求
	 * @param request
	 * @param objectType
	 * @param <T>
	 * @return
	 */
	<T> T postWithMultipart(RichServiceRequest request,Class<T> objectType);

	/**
	 * 使用POST发起multipart请求
	 * @param request
	 * @param <T>
	 * @return
	 */
	<T> T postWithMultipart(RichServiceRequest request);

    /**
     * 使用GET发送服务请求
     * @param objectType
     * @param methodName
     * @param version
     * @param <T>
     * @return
     */
    <T> T get(Class<T> objectType, String methodName, String version);

    /**
     * 使用GET发送ropRequest的请求
     * @param serviceRequest
     * @param objectType
     * @param methodName
     * @param version
     * @param <T>
     * @return
     */
    <T> T get(ServiceRequest serviceRequest, Class<T> objectType, String methodName, String version);

	/**
	 * 使用GET发送ropRequest的请求
	 * @param request
	 * @param objectType
	 * @param <T>
	 * @return
	 */
	<T> T get(RichServiceRequest request, Class<T> objectType);

	/**
	 * 使用GET发送ropRequest的请求
	 * @param request
	 * @param <T>
	 * @return
	 */
	<T> T get(RichServiceRequest request);
}

