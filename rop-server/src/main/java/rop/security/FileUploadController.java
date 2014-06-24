package rop.security;

/**
 * <pre>
 *    文件上传控制器
 * </pre>
 *
 * @author 陈雄华
 * @author luopeng
 * @version 1.0
 */
public interface FileUploadController {

    /**
     * 上传文件的类型是否是允许
     * @param fileType
     * @return
     */
    boolean isAllowFileType(String fileType);

    /**
     * 是否超过了上传大小的限制
     * @param fileSize
     * @return
     */
    boolean isExceedMaxSize(int fileSize);
}

