package rop.request;

/**
 * 富服务请求，带服务方法、版本、返回类信息等
 * @author luopeng
 * Created by luopeng on 14-3-28.
 */
public interface RichServiceRequest extends ServiceRequest {

	/**
	 * 获得服务方法版本
	 * @return
	 */
	String getVersion();

	/**
	 * 获得服务方法名称
	 * @return
	 */
	String getMethod();

	/**
	 * 获得响应类
	 * @return
	 */
	Class<?> getResponseClass();

}
