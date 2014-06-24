package rop.security;


import java.io.Serializable;

/**
 * <pre>
 *   ROP框架子错误类型,定义错误的具体类型
 * </pre>
 *
 * @author 陈雄华
 * @author luopeng
 * @version 1.0
 */
public class SubError implements Serializable {

    private String code;

    private String message;

    public SubError() {
    }

    public SubError(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

