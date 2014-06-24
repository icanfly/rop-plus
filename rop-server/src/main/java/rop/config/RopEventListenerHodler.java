package rop.config;

import rop.event.RopEventListener;

/**
 * <pre>
 * </pre>
 *
 * @author 陈雄华
 * @author luopeng
 * @version 1.0
 */
public class RopEventListenerHodler {

    private RopEventListener ropEventListener;

    public RopEventListenerHodler(RopEventListener ropEventListener) {
        this.ropEventListener = ropEventListener;
    }

    public RopEventListener getRopEventListener() {
        return ropEventListener;
    }
}

