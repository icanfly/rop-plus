package rop.marshaller;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.XppDriver;
import rop.RopMarshaller;

/**
 * XML序列化工具
 * @author luopeng
 * Created by luopeng on 14-3-28.
 */
public class XStreamXmlRopMarshaller implements RopMarshaller {

    private XStream xstream= new XStream(new XppDriver());

	@Override
	public String marshaller(Object object) {
		return xstream.toXML(object);
	}
}
