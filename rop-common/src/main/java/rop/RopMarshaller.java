package rop;

/**
 * <pre>
 *   负责将请求方法返回的{@link rop.response.RopResponse}流化为相应格式的内容。
 * </pre>
 *
 * @author 陈雄华
 * @version 1.0
 */
public interface RopMarshaller {

	String marshaller(Object object);
}

