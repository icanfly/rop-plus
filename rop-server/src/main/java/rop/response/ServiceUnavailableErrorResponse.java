package rop.response;

import rop.error.SubErrors;
import rop.security.MainError;
import rop.security.SubError;
import rop.security.SubErrorType;

import java.util.Locale;

/**
 * 服务不可用时的Rop响应
 * @author luopeng
 */
public class ServiceUnavailableErrorResponse extends FailedRopResponse {

    private static final String ISP = "isp.";

    private static final String SERVICE_UNAVAILABLE = "-service-unavailable";

    //注意，这个不能删除，否则无法进行流化
    public ServiceUnavailableErrorResponse() {
    }

    public ServiceUnavailableErrorResponse(String method, Locale locale) {
        MainError mainError = SubErrors.getMainError(SubErrorType.ISP_SERVICE_UNAVAILABLE, locale);
        String errorCodeKey = ISP + ErrorResponseUtils.transform(method) + SERVICE_UNAVAILABLE;
        SubError subError = SubErrors.getSubError(errorCodeKey,
                SubErrorType.ISP_SERVICE_UNAVAILABLE.value(),
                locale,"", method,"NONE","NONE");
		mainError.addSubError(subError);

        this.setError(mainError);
    }

    public ServiceUnavailableErrorResponse(String method, Locale locale, Throwable throwable) {
        MainError mainError = SubErrors.getMainError(SubErrorType.ISP_SERVICE_UNAVAILABLE, locale);


        String errorCodeKey = ISP + ErrorResponseUtils.transform(method) + SERVICE_UNAVAILABLE;
        Throwable srcThrowable = throwable;
        if(throwable.getCause() != null){
            srcThrowable = throwable.getCause();
        }
        SubError subError = SubErrors.getSubError(errorCodeKey,
                SubErrorType.ISP_SERVICE_UNAVAILABLE.value(),
                locale,"",
                method, srcThrowable.getClass().getName(),getThrowableInfo(throwable));

		mainError.addSubError(subError);
        this.setError(mainError);
    }

    private String getThrowableInfo(Throwable throwable) {
//        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(1024);
//        PrintStream printStream = new PrintStream(outputStream);
//        throwable.printStackTrace(printStream);
//        return outputStream.toString();
		return throwable.getMessage();
    }
}

