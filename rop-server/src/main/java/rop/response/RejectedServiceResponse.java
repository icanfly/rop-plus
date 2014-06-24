package rop.response;

import rop.error.MainErrors;
import rop.security.MainError;
import rop.security.MainErrorType;

import java.util.Locale;

/**
 * 拒绝执行时的Rop响应
 * @author luopeng
 */
public class RejectedServiceResponse extends FailedRopResponse  {

    public RejectedServiceResponse() {
    }

    public RejectedServiceResponse(Locale locale) {
        MainError mainError = MainErrors.getError(MainErrorType.FORBIDDEN_REQUEST, locale);
        this.setError(mainError);
    }
}

