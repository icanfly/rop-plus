package rop.impl;

import rop.security.ServiceAccessController;
import rop.session.Session;

/**
 * <pre>
 *     对调用的方法进行安全性检查
 * </pre>
 *
 * @author 陈雄华
 * @author luopeng
 * @version 1.0
 */
public class DefaultServiceAccessController implements ServiceAccessController {

    @Override
    public boolean isAppGranted(String appKey, String method, String version) {
        return true;
    }

    @Override
    public boolean isUserGranted(Session session, String method, String version) {
        return true;
    }
}

