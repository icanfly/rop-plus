package rop.event;

/**
 * <pre>
 *   检查是否支持特定的事件
 * </pre>
 *
 * @author 陈雄华
 * @version 1.0
 */
public interface SmartRopEventListener extends RopEventListener<RopEvent> {

    /**
     * 是否支持此事件
     *
     * @param eventType
     * @return
     */
    boolean supportsEventType(Class<? extends RopEvent> eventType);
}

