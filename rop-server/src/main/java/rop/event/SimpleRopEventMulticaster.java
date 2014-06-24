package rop.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executor;

/**
 * <pre>
 * 简单实现的ROP事件广播器
 * </pre>
 *
 * @author 陈雄华
 * @version 1.0
 */
public class SimpleRopEventMulticaster extends AbstractRopEventMulticaster {

	protected final Logger logger = LoggerFactory.getLogger(getClass());

	private Executor executor;

	@Override
	public void multicastEvent(final RopEvent event) {
		try {
			for (final RopEventListener listener : getRopEventListeners(event)) {
				Executor executor = getExecutor();
				if (executor != null) {
					executor.execute(new Runnable() {
						@Override
						public void run() {
							listener.onRopEvent(event);
						}
					});
				} else {
					listener.onRopEvent(event);
				}
			}
		} catch (Exception e) {
			logger.error("处理" + event.getClass().getName() + "事件发生异常", e);
		}
	}

	public Executor getExecutor() {
		return executor;
	}

	public void setExecutor(Executor executor) {
		this.executor = executor;
	}
}

