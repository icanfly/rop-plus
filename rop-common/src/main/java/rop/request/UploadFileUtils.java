package rop.request;

import rop.thirdparty.org.apache.commons.codec.binary.Base64;
import rop.thirdparty.org.apache.commons.lang3.StringUtils;
import rop.Constants;

import java.io.UnsupportedEncodingException;

/**
 * <pre>
 *   Rop的上传文件编码格式为：
 *   fileType@BASE64编码的文件内容
 * </pre>
 *
 * @author 陈雄华
 * @author luopeng
 * @version 1.0
 */
public class UploadFileUtils {

    public static final char SPERATOR = '@';

    /**
     * 获取文件名
     *
     * @param encodeFile
     * @return
     */
    public static final String getFileName(String encodeFile) {
        int speratorIndex = encodeFile.indexOf(SPERATOR);
        if (speratorIndex > -1) {
            String encodeFileName = encodeFile.substring(0, speratorIndex);
			try {
				return new String(Base64.decodeBase64(encodeFileName),Constants.UTF8).toLowerCase();
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException(e);
			}
		} else {
            throw new IllegalUploadFileFormatException("文件格式不对，正确格式为：<文件名>@<文件内容>");
        }
    }

	/**
	 * 获取文件类型
	 *
	 * @param encodeFile
	 * @return
	 */
	public static final String getFileType(String encodeFile) {
		String fileName = getFileName(encodeFile);
		if(StringUtils.isEmpty(fileName)){
			throw new IllegalUploadFileFormatException("文件格式不对，正确格式为：<文件名>@<文件内容>");
		}
		return fileName.substring(fileName.lastIndexOf('.')+1);
	}

    /**
     * 获取文件的字节数组
     *
     * @param encodeFile
     * @return
     */
    public static final byte[] decode(String encodeFile) {
        int speratorIndex = encodeFile.indexOf(SPERATOR);
        if (speratorIndex > -1) {
            String content = encodeFile.substring(speratorIndex + 1);
            return Base64.decodeBase64(content);
        } else {
            throw new IllegalUploadFileFormatException("文件格式不对，正确格式为：<文件名>@<文件内容>");
        }
    }

    /**
     * 将文件编码为BASE64的字符串
     *
     * @param bytes
     * @return
     */
    public static final String encode(byte[] bytes) {
        return Base64.encodeBase64URLSafeString(bytes);
    }

	public static final String encode(String src){
		if(StringUtils.isEmpty(src)){
			throw new RuntimeException("encode src is empty");
		}
		try {
			return encode(src.getBytes(Constants.UTF8));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

    /**
     * 将文件编码为一个字符串
     * @param uploadFile
     * @return
     */
    public static final String encode(UploadFile uploadFile){
        StringBuilder sb = new StringBuilder();
        sb.append(encode(uploadFile.getFileName()));
        sb.append(SPERATOR);
        sb.append(encode(uploadFile.getContent()));
        return sb.toString();
    }
}

