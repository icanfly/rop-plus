package rop.annotation;

import java.lang.annotation.*;

/**
 * <pre>验证API接口参数注解</pre>
 * <pre>该注解集成OVAL验证框架</pre>
 * @author luopeng
 *         Created on 2014/4/24.
 */
@Target({ ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ParamValid {

	/**
	 * 验证框架的profile属性，默认为default
	 * @return
	 */
	String[] profiles() default {"default"};
}
