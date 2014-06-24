package rop.security;

import rop.AppkeyResult;
import rop.RopRequestContext;

/**
 * <pre>
 *     Appkey管理器，可根据appKey获取对应的secret.
 * </pre>
 *
 * @author 陈雄华
 * @author luopeng
 * @version 1.0
 */
public interface AppSecretManager {

    /**
     * 获取应用程序的密钥
     *
     * @param appKey
	 * @param requestContext
     * @return
     */
	AppkeyResult getSecret(String appKey, RopRequestContext requestContext);

    /**
     * 是否是合法的appKey
     *
     * @param appKey
	 * @param requestContext
     * @return
     */
	AppkeyResult isValidAppKey(String appKey,RopRequestContext requestContext);
}

