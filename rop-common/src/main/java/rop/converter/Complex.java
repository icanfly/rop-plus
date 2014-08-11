package rop.converter;

import java.lang.annotation.*;

/**
 * 复合参数，如果某个复杂属性被标识，则采用JSON方式将该复杂属性序列化
 * @author luopeng
 *         Created on 2014/7/30.
 */
@Target({ ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Complex {

	Style style() default Style.JSON;
}
