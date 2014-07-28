package rop.client;

import rop.converter.RopConverter;

import java.util.Map;

/**
 * RopClient 抽象
 * @author 陈雄华
 * @author luopeng
 */
public interface RopClient {

    /**
     * 添加自定义的转换器
     *
     * @param ropConverter
     */
    void addRopConvertor(RopConverter ropConverter);

    /**
     * 设置method系统参数的参数名，下同
     *
     * @param paramName
     * @return
     */
    RopClient setAppKeyParamName(String paramName);

    /**
     * 设置sessionId的参数名
     *
     * @param paramName
     * @return
     */
    RopClient setSessionIdParamName(String paramName);

    /**
     * 设置method的参数名
     *
     * @param paramName
     * @return
     */
    RopClient setMethodParamName(String paramName);

    /**
     * 设置version的参数名
     *
     * @param paramName
     * @return
     */
    RopClient setVersionParamName(String paramName);

    /**
     * 设置format的参数名
     *
     * @param paramName
     * @return
     */
    RopClient setFormatParamName(String paramName);

    /**
     * 设置locale的参数名
     *
     * @param paramName
     * @return
     */
    RopClient setLocaleParamName(String paramName);

    /**
     * 设置sign的参数名
     *
     * @param paramName
     * @return
     */
    RopClient setSignParamName(String paramName);

    /**
     * 设置sessionId
     *
     * @param sessionId
     */
    void setSessionId(String sessionId);

	/**
	 * 设置请求时间戳参数名
	 * @param timestampParamName
	 */
	void setTimestampParamName(String timestampParamName);

	/**
	 * 获取扩展信息Map
	 * @return
	 */
	Map<String,String> getExtInfoMap();

    /**
     * 创建一个新的服务请求
     * @return
     */
    ClientRequest buildClientRequest();

	/**
	 * 销毁退出
	 */
	void destroy();
}

