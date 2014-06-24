package rop.security;

import java.util.ArrayList;
import java.util.List;

/**
 * <pre>
 *    1.如果maxSize为非正数，则表示不限制大小；
 *    2.如果allowAllTypes为true表示不限制文件类型；
 * </pre>
 *
 * @author 陈雄华
 * @author luopeng
 * @version 1.0
 */
public class DefaultFileUploadController implements FileUploadController {

    private List<String> fileTypes;
    
    private int maxSize = -1;

    private boolean allowAllTypes = false;

    public DefaultFileUploadController(int maxSize) {
        this.allowAllTypes = true;
        this.maxSize = maxSize;
    }

    public DefaultFileUploadController(List<String> fileTypes, int maxSize) {
        ArrayList<String> tempFileTypes = new ArrayList<String>(fileTypes.size());
        for (String fileType : fileTypes) {
            tempFileTypes.add(fileType.toLowerCase());
        }
        this.fileTypes = tempFileTypes;
        this.maxSize = maxSize;
    }

    @Override
    public boolean isAllowFileType(String fileType) {
        if(allowAllTypes){
            return true;
        }else{
            if(fileType == null){
                return false;
            }else{
                fileType = fileType.toLowerCase();
                return fileTypes.contains(fileType);
            }
        }
    }

    @Override
    public boolean isExceedMaxSize(int fileSize) {
        if(maxSize > 0){
            return fileSize > maxSize * 1024;
        }else{
            return false;
        }
    }
}

