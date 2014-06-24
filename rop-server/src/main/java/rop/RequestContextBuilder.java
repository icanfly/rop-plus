package rop;

import org.springframework.validation.SmartValidator;

/**
 *  更改请求对象创建{@link RopRequestContext}实例,子类可以根据多种传输协议定义自己的创建器。
 *  @author 陈雄华
 */
public interface RequestContextBuilder {

    /**
     * 根据reqeuest请求对象，创建{@link RopRequestContext}实例。绑定系统参数，请求对象
     *
     * @param ropContext
     * @param request
     * @return
     */
    RopRequestContext buildSystemParams(RopContext ropContext, Object request);

    /**
     * 绑定业务参数
     *
     * @param ropRequestContext
     */
    void bindBusinessParams(RopRequestContext ropRequestContext);

	/**
	 * 设置验证器
	 * @param validator
	 */
	void setValidator(SmartValidator validator);
}

