package rop.request;

import rop.utils.spring.FileCopyUtils;

import java.io.File;
import java.io.IOException;

/**
 * <pre>
 *   文件上传对象
 * </pre>
 *
 * @author 陈雄华
 * @author luopeng
 * @version 1.0
 */
//@IgnoreSign
public class UploadFile {

    private String fileType;

	private String fileName;

    private byte[] content;

    /**
     * 根据文件内容构造
     *
     * @param content
     */
    public UploadFile(String fileName, byte[] content) {
		assert fileName != null;
        this.content = content;
        this.fileName = fileName;
		this.fileType = fileName.substring(fileName.lastIndexOf('.') + 1);
    }

    /**
     * 根据文件构造
     * @param file
     */
    public UploadFile(File file) {
        try {
            this.content = FileCopyUtils.copyToByteArray(file);
			this.fileName = file.getName();
            this.fileType = file.getName().substring(file.getName().lastIndexOf('.')+1);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getFileType() {
        return fileType;
    }

    public byte[] getContent() {
        return content;
    }

	public String getFileName() {
		return fileName;
	}
}


