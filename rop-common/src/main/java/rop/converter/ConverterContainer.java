package rop.converter;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author luopeng
 *         Created on 2014/6/20.
 */
public class ConverterContainer {

	private Map<Class<?>,RopConverter<?>> converters = new HashMap<Class<?>,RopConverter<?>>(2);

	public void addConverter(RopConverter<?> converter){
		if(converter != null){
			converters.put(converter.getSupportClass(),converter);
		}
	}

	public boolean support(Class<?> classType){
		return converters.containsKey(classType);
	}

	public <T> RopConverter<T> getConverter(Class<T> classType){
		return (RopConverter<T>) converters.get(classType);
	}

}
