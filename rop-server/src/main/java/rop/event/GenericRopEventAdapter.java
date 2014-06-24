package rop.event;

import org.springframework.aop.support.AopUtils;
import org.springframework.core.GenericTypeResolver;

/**
 * <pre>
 *
 * </pre>
 *
 * @author 陈雄华
 * @author luopeng
 * @version 1.0
 */
public class GenericRopEventAdapter implements SmartRopEventListener {

    private final RopEventListener delegate;

    public GenericRopEventAdapter(RopEventListener delegate) {
        this.delegate = delegate;
    }

    @Override
    public boolean supportsEventType(Class<? extends RopEvent> eventType) {
        Class typeArg = GenericTypeResolver.resolveTypeArgument(this.delegate.getClass(), RopEventListener.class);
        if (typeArg == null || typeArg.equals(RopEvent.class)) {
            Class targetClass = AopUtils.getTargetClass(this.delegate);
            if (targetClass != this.delegate.getClass()) {
                typeArg = GenericTypeResolver.resolveTypeArgument(targetClass, RopEventListener.class);
            }
        }
        return (typeArg == null || typeArg.isAssignableFrom(eventType));
    }

    @Override
    public void onRopEvent(RopEvent ropEvent) {
        this.delegate.onRopEvent(ropEvent);
    }

    @Override
    public int getOrder() {
        return Integer.MAX_VALUE;
    }
}

