package rop.response;

import rop.error.SubErrors;
import rop.security.MainError;
import rop.security.SubError;
import rop.security.SubErrorType;

import java.util.Locale;

/**
 * <pre>
 * </pre>
 *
 * @author 陈雄华
 * @author luopeng
 * @version 1.0
 */
public class NotExistErrorResponse extends FailedRopResponse {

    public static final String ISV = "isv.";
    public static final String NOT_EXIST_INVALID = "-not-exist:invalid-";

    //注意，这个不能删除，否则无法进行流化
    public NotExistErrorResponse() {
    }

    /**
     * 对象不存在的错误对象。当根据<code>queryFieldName</code>查询<code>objectName</code>时，查不到记录，则返回该错误对象。
     *
     * @param objectName     对象的名称
     * @param queryFieldName 查询字段的名称
     * @param locale         本地化对象
     */
    public NotExistErrorResponse(String objectName, String queryFieldName, Object queryFieldValue, Locale locale) {
        MainError mainError = SubErrors.getMainError(SubErrorType.ISV_NOT_EXIST, locale);
        String subErrorCode = SubErrors.getSubErrorCode(SubErrorType.ISV_NOT_EXIST, objectName, queryFieldName);

        SubError subError = SubErrors.getSubError(subErrorCode, SubErrorType.ISV_NOT_EXIST.value(), locale, "",
				queryFieldName, queryFieldValue, objectName);
        mainError.addSubError(subError);

        this.setError(mainError);
    }
}

