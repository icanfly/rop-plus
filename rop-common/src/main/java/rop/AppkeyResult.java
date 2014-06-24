package rop;

/**
 * Appkey Result
 * @author luopeng
 *         Created on 2014/6/16.
 */
public class AppkeyResult extends Result<String> {

	public static final String APPKEY_INACTIVE = "APPKEY_INACTIVE";//Appkey未激活

	/**
	 * 调用者ID，即Appkey对应的用户id
	 */
	private Object appkeyUserId;

	public AppkeyResult(Object appkeyUserId){
		this.setAppkeyUserId(appkeyUserId);
	}

	public Object getAppkeyUserId() {
		return appkeyUserId;
	}

	public void setAppkeyUserId(Object appkeyUserId) {
		this.appkeyUserId = appkeyUserId;
	}

	public String getSecret(){
		return this.getData();
	}

}
