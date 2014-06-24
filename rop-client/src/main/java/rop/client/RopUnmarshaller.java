package rop.client;

/**
 *  ROP框架反序列化接口
 * @author 陈雄华
 * @author luopeng
 */
public interface RopUnmarshaller {

    /**
     * 将字符串反序列化为相应的对象
     *
     * @param content
     * @param objectType
     * @return
     */
   <T> T unmarshaller(String content, Class<T> objectType);
}

