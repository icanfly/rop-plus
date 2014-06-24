package rop.annotation;

/**
 * <pre>
 * 功能说明：是否需求会话检查
 * </pre>
 *
 * @author 陈雄华
 * @author luopeng
 * @version 1.0
 */
public enum NeedInSessionType {
    YES, NO, DEFAULT;

    public static boolean isNeedInSession(NeedInSessionType type) {
        if (YES == type /*|| DEFAULT == type*/) {
            return true;
        } else {
            return false;
        }
    }
}

