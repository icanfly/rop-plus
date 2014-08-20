package rop.json;

import rop.RopUnmarshaller;
import rop.thirdparty.com.alibaba.fastjson.JSON;

/**
 * Fastjson 反序列化
 * @author luopeng
 * on 14-3-27.
 */
public class FastjsonRopUnmarshaller implements RopUnmarshaller {

	@Override
	public <T> T unmarshaller(String content, Class<T> objectType) {
		return JSON.parseObject(content, objectType);
	}
}
