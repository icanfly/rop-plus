package rop.event;

/**
 * <pre>
 *   注册事件监听器，发布事件
 * </pre>
 *
 * @author 陈雄华
 * @version 1.0
 */
public interface RopEventMulticaster {

    /**
     * Add a listener to be notified of all events.
     *
     * @param listener the listener to add
     */
    void addRopListener(RopEventListener listener);

    /**
     * Remove a listener from the notification list.
     *
     * @param listener the listener to remove
     */
    void removeRopListener(RopEventListener listener);

    /**
     * Remove all listeners registered with this multicaster.
     * <p>After a remove call, the multicaster will perform no action
     * on event notification until new listeners are being registered.
     */
    void removeAllRopListeners();

    /**
     * Multicast the given application event to appropriate listeners.
     *
     * @param event the event to multicast
     */
    void multicastEvent(RopEvent event);
}

