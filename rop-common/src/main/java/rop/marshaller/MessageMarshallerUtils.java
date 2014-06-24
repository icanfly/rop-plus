package rop.marshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rop.MessageFormat;
import rop.RopMarshaller;

import java.util.Map;

/**
 * <pre>
 *   对请求响应的对象转成相应的报文。
 * </pre>
 *
 * @author 陈雄华
 * @author luopeng
 * @version 1.0
 */
public class MessageMarshallerUtils {

	protected static final Logger logger = LoggerFactory.getLogger(MessageMarshallerUtils.class);

	private static final String UTF_8 = "utf-8";

	private static RopMarshaller jsonMarshaller = new FastjsonRopMarshaller();

	private static RopMarshaller xmlMarshaller = new XStreamXmlRopMarshaller();

	private static RopMarshaller getMarshaller(MessageFormat format) {
		RopMarshaller marshaller = null;
		if (format == MessageFormat.json) {
			marshaller = jsonMarshaller;
		} else if (format == MessageFormat.xml) {
			marshaller = xmlMarshaller;
		} else {
			throw new RuntimeException("Not supported message format.");
		}
		return marshaller;
	}

	/**
	 * 将请求对象转换为String
	 *
	 * @param object
	 * @param format
	 * @return
	 */
	public static String getMessage(Object object, MessageFormat format) {
		return getMarshaller(format).marshaller(object);
	}

	/**
	 * 将请求对象转换为String
	 *
	 * @param params
	 * @return
	 */
	public static String asUrlString(Map<String, String> params) {
		StringBuilder sb = new StringBuilder(256);
		boolean first = true;
		for (Map.Entry<String, String> entry : params.entrySet()) {
			if (!first) {
				sb.append("&");
			}
			first = false;
			sb.append(entry.getKey());
			sb.append("=");
			sb.append(entry.getValue());
		}
		return sb.toString();
	}
}

