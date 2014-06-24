package rop.security;

import rop.session.Session;

/**
 * <pre>
 *    服务访问控制器，决定用户是否有权访问服务。
 * </pre>
 * @author luopeng
 * @author 陈雄华
 */
public interface ServiceAccessController {

    /**
     * 服务方法是否向ISV开放
     * @param appKey
     * @param method
	 * @param version
     * @return
     */
    boolean isAppGranted(String appKey, String method, String version);

    /**
     *  服务方法是否向当前用户开放
     * @param session
	 * @param method
	 * @param version
     * @return
     */
    boolean isUserGranted(Session session, String method, String version);
}

