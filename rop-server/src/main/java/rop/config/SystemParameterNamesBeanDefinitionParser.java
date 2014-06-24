package rop.config;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;
import rop.request.SystemParameterNames;

/**
 * <pre>
 *     指定自定义的系统参数名
 * </pre>
 *
 * @author 陈雄华
 * @author luopeng
 * @version 1.0
 */
public class SystemParameterNamesBeanDefinitionParser implements BeanDefinitionParser {
    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        String appKey = element.getAttribute("appkey-param-name");
        String sessionId = element.getAttribute("sessionid-param-name");
        String method = element.getAttribute("method-param-name");
        String version = element.getAttribute("version-param-name");
        String format = element.getAttribute("format-param-name");
        String locale = element.getAttribute("locale-param-name");
        String sign = element.getAttribute("sign-param-name");
        String jsonp = element.getAttribute("jsonp-param-name");
		String timestamp = element.getAttribute("timestamp-param-name");

        if (StringUtils.hasText(appKey)) {
            SystemParameterNames.setAppKey(appKey);
        }
        if (StringUtils.hasText(sessionId)) {
            SystemParameterNames.setSessionId(sessionId);
        }
        if (StringUtils.hasText(method)) {
            SystemParameterNames.setMethod(method);
        }
        if (StringUtils.hasText(version)) {
            SystemParameterNames.setVersion(version);
        }
        if (StringUtils.hasText(format)) {
            SystemParameterNames.setFormat(format);
        }
        if (StringUtils.hasText(locale)) {
            SystemParameterNames.setLocale(locale);
        }
        if (StringUtils.hasText(sessionId)) {
            SystemParameterNames.setSign(sign);
        }
        if (StringUtils.hasText(jsonp)) {
            SystemParameterNames.setJsonp(jsonp);
        }
		if(StringUtils.hasText(timestamp)){
			SystemParameterNames.setTimestamp(timestamp);
		}


        return null;
    }
}

