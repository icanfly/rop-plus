package rop.session;

import java.util.HashMap;
import java.util.Map;

/**
 * <pre>
 * </pre>
 * @author luopeng
 * @author 陈雄华
 */
public  abstract class AbstractSession implements Session {

    private Map<String, Object> attributes = new HashMap<String, Object>();

    public void setAttribute(String name, Object obj) {
        attributes.put(name, obj);
    }

    public Object getAttribute(String name) {
        return attributes.get(name);
    }
}

