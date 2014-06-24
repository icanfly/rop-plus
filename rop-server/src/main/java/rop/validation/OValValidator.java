package rop.validation;

import net.sf.oval.ConstraintViolation;
import net.sf.oval.configuration.xml.XMLConfigurer;
import net.sf.oval.context.FieldContext;
import net.sf.oval.context.OValContext;
import net.sf.oval.guard.Guard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.validation.Errors;
import org.springframework.validation.SmartValidator;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Oval验证框架的spring集成
 *
 * @author luopeng
 *         Created on 2014/4/24.
 */
public class OValValidator implements SmartValidator, InitializingBean {

	private Logger logger = LoggerFactory.getLogger(OValValidator.class);

	private Guard guard;

	/**
	 * 配置文件位置
	 */
	private Resource[] validatorConfigLocations;

	@Override
	public boolean supports(Class<?> clazz) {
		return true;
	}

	@Override
	public void validate(Object target, Errors errors) {
		validate(target, errors, null);
	}

	@Override
	public void validate(Object target, Errors errors, Object... validationHints) {
		long start = System.currentTimeMillis();
		List<ConstraintViolation> violations = Collections.EMPTY_LIST;
		if (validationHints == null) {
			violations = guard.validate(target);
		} else {
			String[] profiles = (String[]) validationHints;
			violations = guard.validate(target, profiles);
		}

		if (!violations.isEmpty()) {
			for (ConstraintViolation violation : violations) {
				OValContext context = violation.getContext();
				if (context instanceof FieldContext) {
					FieldContext fieldContext = (FieldContext) context;
					errors.rejectValue(fieldContext.getField().getName(), violation.getErrorCode(),
							violation.getMessage());
				} else {
					errors.reject(violation.getErrorCode(), violation.getMessage());
				}
			}
		}
		long end = System.currentTimeMillis();
		if(logger.isDebugEnabled()){
			logger.debug("validation spend："+(end - start) +"ms, Class:"+target.getClass().getCanonicalName()+"," +
						 "Profiles:"+ (validationHints == null ? "[default]" : Arrays.asList(validationHints)));
		}
	}

	public static void main(String[] args) {
		System.out.println(OValValidator.class.getCanonicalName());
	}

	public Resource[] getValidatorConfigLocations() {
		return validatorConfigLocations;
	}

	public void setValidatorConfigLocations(Resource[] validatorConfigLocations) {
		this.validatorConfigLocations = validatorConfigLocations;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (this.validatorConfigLocations == null || this.validatorConfigLocations.length == 0) {
			guard = new Guard();
			logger.warn("validatorConfigLocations is null, no validator registered!");
			return;
		}
		List<XMLConfigurer> configurers = new LinkedList<XMLConfigurer>();
		for (Resource resource : validatorConfigLocations) {
			XMLConfigurer configurer = new XMLConfigurer(resource.getInputStream());
			configurers.add(configurer);
		}
		guard = new Guard(configurers.toArray(new XMLConfigurer[configurers.size()]));
	}

}
