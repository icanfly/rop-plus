package rop.converter;

public interface RopConverter<T> {

	/**
	 * 从T转换成String
	 * @param object
	 * @return
	 */
	String convertToString(T object);

    /**
     * 从String转换成T
     * @param object
     * @return
     */
    T convertToObject(String object);

    /**
     * 获取支持的转换类型
     * @return
     */
    Class<T> getSupportClass();

}

