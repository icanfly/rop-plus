package rop;

import org.apache.commons.fileupload.FileItem;
import rop.annotation.HttpAction;
import rop.response.RopResponse;
import rop.session.Session;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 接到服务请求后，将产生一个{@link rop.RopRequestContext}上下文对象，它被本次请求直到返回响应的这个线程共享。
 * @author 陈雄华
 * @version 1.0
 */
public interface RopRequestContext {

	/**
	 * 获取Rop的上下文
	 *
	 * @return
	 */
	RopContext getRopContext();

	/**
	 * 获取服务的方法
	 *
	 * @return
	 */
	String getMethod();

	/**
	 * 获取服务的版本号
	 *
	 * @return
	 */
	String getVersion();

	/**
	 * 获取应用的appKey
	 *
	 * @return
	 */
	String getAppKey();

	/**
	 * 获取会话的ID
	 *
	 * @return
	 */
	String getSessionId();

	/**
	 * 获取请求所对应的会话
	 *
	 * @return
	 */
	Session getSession();

	/**
	 * 绑定一个会话
	 *
	 * @param session
	 */
	void addSession(String sessionId, Session session);

	/**
	 * 删除会话，删除{@link #getSessionId()}对应的Session
	 */
	void removeSession();

	/**
	 * 获取报文格式参数
	 *
	 * @return
	 */
	String getFormat();

	/**
	 * 获取本地化对象
	 *
	 * @return
	 */
	Locale getLocale();

	/**
	 * 获取签名
	 *
	 * @return
	 */
	String getSign();

	/**
	 * 获取客户端的IP
	 *
	 * @return
	 */
	String getIp();

	/**
	 * 获取请求的方法 如POST
	 */
	HttpAction getHttpAction();

	/**
	 * 获取请求时间戳
	 *
	 * @return
	 */
	long getTimestamp();

	/**
	 * 获取请求的原对象（如HttpServletRequest）
	 *
	 * @return
	 */
	Object getRawRequestObject();

	/**
	 * 设置服务开始时间
	 *
	 * @param serviceBeginTime
	 */
	void setServiceBeginTime(long serviceBeginTime);

	/**
	 * 获取服务开始时间，单位为毫秒，为-1表示无意义
	 *
	 * @return
	 */
	long getServiceBeginTime();

	/**
	 * 设置服务开始时间
	 *
	 * @param serviceEndTime
	 */
	void setServiceEndTime(long serviceEndTime);

	/**
	 * 获取服务结束时间，单位为毫秒，为-1表示无意义
	 *
	 * @return
	 */
	long getServiceEndTime();

	/**
	 * 获取服务方法对应的ApiMethod对象信息
	 *
	 * @return
	 */
	ServiceMethodDefinition getServiceMethodDefinition();

	/**
	 * 获取服务的处理者
	 *
	 * @return
	 */
	ServiceMethodHandler getServiceMethodHandler();


	/**
	 * 注意多线程情况
	 *
	 * @param ropResponse
	 */
	void setRopResponse(RopResponse ropResponse);

	/**
	 * 返回响应对象
	 * 注意多线程情况
	 *
	 * @return
	 */
	RopResponse getRopResponse();

	/**
	 * 获取参数
	 *
	 * @return
	 */
	Object[] getServiceMethodParameters();

	/**
	 * 设置参数
	 *
	 * @param parameters
	 */
	void setServiceMethodParameters(Object[] parameters);

	/**
	 * 获取特定属性
	 *
	 * @param name
	 * @return
	 */
	Object getAttribute(String name);

	/**
	 * 设置属性的值
	 *
	 * @param name
	 * @param value
	 */
	void setAttribute(String name, Object value);

	/**
	 * 该方法是否开启签名的功能
	 *
	 * @return
	 */
	boolean isSignEnable();

	/**
	 * 获取请求参数列表
	 *
	 * @return
	 */
	Map<String, String> getRequestBodyMap();

	/**
	 * 获取系统head参数
	 *
	 * @return
	 */
	Map<String, String> getRequestHeaderMap();

	/**
	 * 获取请求参数值
	 *
	 * @param paramName
	 * @return
	 */
	String getBodyParameter(String paramName);

	/**
	 * 获取请求ID，是一个唯一的UUID，每次请求对应一个唯一的ID
	 *
	 * @return
	 */
	String getRequestId();

	/**
	 * 获取文件上传Item
	 *
	 * @return
	 */
	List<FileItem> getFileItems();

	/**
	 * 设置appkey对应的用户id
	 *
	 * @param appkeyUserId
	 */
	void setAppkeyUserId(Object appkeyUserId);

	/**
	 * 获取appkey对应的用户id
	 *
	 * @return
	 */
	Object getAppkeyUserId();

	/**
	 * 设置ROP请求ID
	 * @param requestId
	 */
	void setRequestId(String requestId);

	/**
	 * 获取额外的自定义信息Map
	 * @return
	 */
	Map<String,String> getExtInfoMap();

	/**
	 * 设置额外的自定义信息Map
	 * @param extInfoMap
	 */
	void setExtInfoMap(Map<String,String> extInfoMap);

	/**
	 * 获取额外的自定义信息
	 * @param extKey
	 * @return
	 */
	String getExtInfo(String extKey);
}

