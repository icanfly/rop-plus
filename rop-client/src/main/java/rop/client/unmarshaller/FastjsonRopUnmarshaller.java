package rop.client.unmarshaller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.parser.Feature;
import rop.client.RopUnmarshaller;
import rop.response.RopResponse;

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
