package rop.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rop.RopRequestContext;
import rop.ServiceMethodAdapter;
import rop.ServiceMethodHandler;
import rop.response.RopResponse;

import java.lang.reflect.InvocationTargetException;

/**
 * <pre>
 *     通过该服务方法适配器调用目标的服务方法
 * </pre>
 *
 * @author 陈雄华
 * @author luopeng
 * @version 1.0
 */
public class AnnotationServiceMethodAdapter implements ServiceMethodAdapter {

	protected final Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * 调用ROP服务方法
	 *
	 * @param ropRequestContext
	 * @return
	 */
	public RopResponse invokeServiceMethod(RopRequestContext ropRequestContext) {
		try {
			ServiceMethodHandler serviceMethodHandler = ropRequestContext.getServiceMethodHandler();
			if (logger.isDebugEnabled()) {
				logger.debug("执行" + serviceMethodHandler.getHandler().getClass() +
							 "." + serviceMethodHandler.getHandlerMethod().getName());
			}
			return (RopResponse) serviceMethodHandler.getHandlerMethod().invoke(
					serviceMethodHandler.getHandler(), ropRequestContext.getServiceMethodParameters());
		} catch (Throwable e) {
			if (e instanceof InvocationTargetException) {
				InvocationTargetException inve = (InvocationTargetException) e;
				throw new RuntimeException(inve.getTargetException());
			} else {
				throw new RuntimeException(e);
			}
		}
	}

}

