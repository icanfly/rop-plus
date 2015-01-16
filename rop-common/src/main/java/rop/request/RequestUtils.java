package rop.request;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rop.annotation.IgnoreSign;
import rop.annotation.Temporary;
import rop.utils.spring.AnnotationUtils;
import rop.utils.spring.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 请求工具类
 * Created by luopeng on 14-3-24.
 */
public class RequestUtils {

	private static Logger logger = LoggerFactory.getLogger(RequestUtils.class);

	public static Set<String> getIgnoreSignFieldNames(Class<?> classType) {
		final Set<String> igoreSignFieldNames = new HashSet<String>(1);
		igoreSignFieldNames.add(SystemParameterNames.getSign());
		igoreSignFieldNames.addAll(_getIgnoreSignFieldNames(classType));
		return igoreSignFieldNames;
	}

	public static Set<String> getIgnoreSignFieldNames(List<Class<?>> classTypes) {
		final Set<String> igoreSignFieldNames = new HashSet<String>(1);
		igoreSignFieldNames.add(SystemParameterNames.getSign());
		for(Class<?> classType : classTypes){
			igoreSignFieldNames.addAll(_getIgnoreSignFieldNames(classType));
		}
		return igoreSignFieldNames;
	}

	private static Set<String> _getIgnoreSignFieldNames(Class<?> classType) {
		final Set<String> igoreSignFieldNames = new HashSet<String>(1);
		if (classType != null) {
			if (logger.isDebugEnabled()) {
				logger.debug("获取" + classType.getCanonicalName() + "不需要签名的属性");
			}
			ReflectionUtils.doWithFields(classType, new ReflectionUtils.FieldCallback() {
						public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
							igoreSignFieldNames.add(field.getName());
						}
					},
					new ReflectionUtils.FieldFilter() {
						public boolean matches(Field field) {

							//属性类标注了@IgnoreSign
							IgnoreSign typeIgnore = AnnotationUtils.findAnnotation(field.getType(), IgnoreSign.class);

							//属性定义处标注了@IgnoreSign
							IgnoreSign varIgnoreSign = field.getAnnotation(IgnoreSign.class);

							//属性定义处标注了@Temporary
							Temporary varTemporary = field.getAnnotation(Temporary.class);

							return typeIgnore != null || varIgnoreSign != null || varTemporary != null;
						}
					}
			);
			if (igoreSignFieldNames.size() > 1 && logger.isDebugEnabled()) {
				logger.debug(classType.getCanonicalName() + "不需要签名的属性:" + igoreSignFieldNames.toString());
			}
		}
		return igoreSignFieldNames;
	}
}
