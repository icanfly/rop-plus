package rop.marshaller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import rop.RopMarshaller;

/**
 * JSON 序列化
 * Created by luopeng on 14-3-27.
 */
public class FastjsonRopMarshaller implements RopMarshaller {

	@Override
	public String marshaller(Object object) {
		return JSON.toJSONString(object,SerializerFeature.DisableCircularReferenceDetect);
	}
}
