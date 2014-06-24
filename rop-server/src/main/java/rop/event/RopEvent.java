package rop.event;

import rop.RopContext;

import java.util.EventObject;

/**
 * <pre>
 *
 * </pre>
 *
 * @author 陈雄华
 * @author luopeng
 * @version 1.0
 */
public abstract class RopEvent extends EventObject {

    private RopContext ropContext;

	/**
	 * 事件发生的时间
	 * add by luopeng 2014-06-22
	 */
	private long eventTimestamp;

    public RopEvent(Object source, RopContext ropContext) {
        super(source);
        this.ropContext = ropContext;
		this.eventTimestamp = System.currentTimeMillis();
    }

    public RopContext getRopContext() {
        return ropContext;
    }

	public long getEventTimestamp() {
		return eventTimestamp;
	}
}

