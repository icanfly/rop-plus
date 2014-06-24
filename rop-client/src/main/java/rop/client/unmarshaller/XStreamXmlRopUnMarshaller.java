package rop.client.unmarshaller;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.XppDriver;
import rop.RopException;
import rop.client.RopUnmarshaller;
import rop.response.RopResponse;

/**
 * XML 反序列化
 * @author luopeng
 * on 14-3-27.
 */
public class XStreamXmlRopUnMarshaller implements RopUnmarshaller {

	private XStream xstream= new XStream(new XppDriver());

	@Override
	public <T> T unmarshaller(String content, Class<T> objectType) {
		try {
			Object result = objectType.newInstance();
			return (T) xstream.fromXML(content,result);
		} catch (InstantiationException e) {
			throw new RopException(e);
		} catch (IllegalAccessException e) {
			throw new RopException(e);
		}
	}
}
