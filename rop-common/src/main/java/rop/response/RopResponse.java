package rop.response;

import rop.RopException;
import rop.security.MainError;

import java.io.Serializable;

/**
 * ROP框架统一响应基类
 * @author luopeng
 * Created by luopeng on 14-3-27.
 */
public class RopResponse implements Serializable {

	private boolean success;

	private MainError error;

	public static <T extends RopResponse> T create(Class<? extends RopResponse> responseClass){

		T response = null;
		try {
			response = (T) responseClass.newInstance();
			response.setSuccess(false);
			return response;
		} catch (InstantiationException e) {
			throw new RopException(e);
		} catch (IllegalAccessException e) {
			throw new RopException(e);
		}
	}

	public <T extends RopResponse> T success(){
		this.setSuccess(true);
		return (T) this;
	}

	public <T extends RopResponse> T fail(){
		this.setSuccess(false);
		return (T) this;
	}

	public <T extends RopResponse> T fail(MainError error){
		this.setSuccess(false);
		this.setError(error);
		return (T) this;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public MainError getError() {
		return error;
	}

	public void setError(MainError error) {
		this.error = error;
	}
}
