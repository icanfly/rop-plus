package rop.security;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * <pre>
 *   ROP框架主要错误类型,定义错误的主要类型
 * </pre>
 *
 * @author 陈雄华
 * @author luopeng
 * @version 1.0
 */
public class MainError implements Serializable {

	private String code;

	private String message;

	private String solution;

	private List<SubError> subErrors = new ArrayList<SubError>();

	//必须要有空构造函数以帮助反序列化
	public MainError(){

	}

	public MainError(String code, String message, String solution) {
		this.code = code;
		this.message = message;
		this.solution = solution;
	}

	public String getCode() {
		return code;
	}

	public String getMessage() {
		return message;
	}

	public String getSolution() {
		return solution;
	}

	public List<SubError> getSubErrors() {
		return this.subErrors;
	}

	public void setSubErrors(List<SubError> subErrors) {
		this.subErrors = subErrors;
	}

	public MainError addSubError(SubError subError) {
		this.subErrors.add(subError);
		return this;
	}

}

