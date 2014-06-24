package rop.response;

import rop.error.SubErrors;
import rop.security.MainError;
import rop.security.SubError;
import rop.security.SubErrorType;

import java.util.Locale;

/**
 * 服务超时的Rop响应
 * @author luopeng
 */
public class ServiceTimeoutErrorResponse extends FailedRopResponse {

    private static final String ISP = "isp.";

    private static final String SERVICE_TIMEOUT = "-service-timeout";

    public ServiceTimeoutErrorResponse() {
    }

    public ServiceTimeoutErrorResponse(String method, Locale locale, int timeout) {
        MainError mainError = SubErrors.getMainError(SubErrorType.ISP_SERVICE_TIMEOUT, locale);

        String errorCodeKey = ISP + ErrorResponseUtils.transform(method) + SERVICE_TIMEOUT;
        SubError subError = SubErrors.getSubError(errorCodeKey,
                SubErrorType.ISP_SERVICE_TIMEOUT.value(),
                locale,"",
                method, timeout);
        mainError.addSubError(subError);
        this.setError(mainError);
    }

}

