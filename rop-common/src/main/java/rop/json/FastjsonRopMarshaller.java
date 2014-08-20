package rop.json;

import rop.RopMarshaller;
import rop.thirdparty.com.alibaba.fastjson.JSON;
import rop.thirdparty.com.alibaba.fastjson.serializer.SerializerFeature;

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
