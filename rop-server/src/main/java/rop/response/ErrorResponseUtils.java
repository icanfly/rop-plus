package rop.response;

import rop.error.MainErrors;
import rop.error.SubErrors;
import rop.security.MainError;
import rop.security.MainErrorType;
import rop.security.SubError;
import rop.security.SubErrorType;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Locale;

/**
 * 错误响应帮助类，主要作用是帮助生成{@link rop.response.RopResponse}
 * @author luopeng
 */
public class ErrorResponseUtils {

    private static final String ISV = "isv.";

    private static final String SERVICE_ERROR = "-service-error:";

	private static final String ISP = "isp.";

	private static final String SERVICE_UNAVAILABLE = "-service-unavailable";

    /**
     * 服务发生错误的错误响应，错误码的格式为：isv.***-service-error:###,假设
     * serviceName为book.upload，error_code为INVLIAD_USERNAME_OR_PASSWORD，则错误码会被格式化为：
     * isv.book-upload-service-error:INVLIAD_USERNAME_OR_PASSWORD
     *
     * @param serviceName 服务名，如book.upload,会被自动转换为book-upload
     * @param errorCode   错误的代码，如INVLIAD_USERNAME_OR_PASSWORD,在错误码的后面，一般为大写或数字。
     * @param locale      本地化对象
     * @param params      错误信息的参数，如错误消息的值为this is a {0} error，则传入的参数为big时，错误消息格式化为：
     *                    this is a big error
     */
    public static RopResponse buildServiceErrorResponse(String serviceName, String errorCode, Locale locale, Object... params) {

		RopResponse response = new RopResponse();
		response.setSuccess(false);

        MainError mainError = MainErrors.getError(MainErrorType.BUSINESS_LOGIC_ERROR, locale);

        serviceName = transform(serviceName);
        String subErrorCode = ISV + serviceName + SERVICE_ERROR + errorCode;
        SubError subError = SubErrors.getSubError(subErrorCode, subErrorCode, locale, "发生了错误", params);
		mainError.addSubError(subError);

		response.setError(mainError);

        return response;
    }

	public static RopResponse buildErrorResponse(MainError error){
		RopResponse response = new RopResponse();
		response.setSuccess(false);
		response.setError(error);
		return response;
	}

	public static RopResponse buildServiceUnavailableErrorResponse(String method, Locale locale) {
		RopResponse response = new RopResponse();

		MainError mainError = SubErrors.getMainError(SubErrorType.ISP_SERVICE_UNAVAILABLE, locale);
		String errorCodeKey = ISP + transform(method) + SERVICE_UNAVAILABLE;
		SubError subError = SubErrors.getSubError(errorCodeKey,
				SubErrorType.ISP_SERVICE_UNAVAILABLE.value(),
				locale,"发生了错误",method,"NONE","NONE");
		mainError.addSubError(subError);
        response.setError(mainError);
		return response;
	}

	public static RopResponse buildServiceUnavailableErrorResponse(String method, Locale locale, Throwable throwable) {
		RopResponse response = new RopResponse();
		MainError mainError = SubErrors.getMainError(SubErrorType.ISP_SERVICE_UNAVAILABLE, locale);

		ArrayList<SubError> subErrors = new ArrayList<SubError>();

		String errorCodeKey = ISP + transform(method) + SERVICE_UNAVAILABLE;
		Throwable srcThrowable = throwable;
		if(throwable.getCause() != null){
			srcThrowable = throwable.getCause();
		}
		SubError subError = SubErrors.getSubError(errorCodeKey,
				SubErrorType.ISP_SERVICE_UNAVAILABLE.value(),
				locale,"发生了错误",
				method, srcThrowable.getClass().getName(),getThrowableInfo(throwable));
		mainError.addSubError(subError);
        response.setError(mainError);
		return response;
	}

	private static String getThrowableInfo(Throwable throwable) {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream(1024);
		PrintStream printStream = null;
		try {
			printStream = new PrintStream(outputStream,true,"utf-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		throwable.printStackTrace(printStream);
		try {
			return outputStream.toString("utf-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	public static String transform(String method) {
		if(method != null){
			method = method.replace(".", "-");
			return method;
		}else{
			return "LACK_METHOD";
		}
	}

}

