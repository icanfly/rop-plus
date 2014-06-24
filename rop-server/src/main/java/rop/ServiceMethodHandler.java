
package rop;

import com.google.common.collect.Lists;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;


public class ServiceMethodHandler {

    //处理器对象
    private Object handler;

    //处理器的处理方法
    private Method handlerMethod;

    private ServiceMethodDefinition serviceMethodDefinition;

    //无需签名的字段列表
    private Set<String> ignoreSignFieldNames;

    //属性类型为FileItem的字段列表
    private List<String> uploadFileFieldNames;

	//方法参数列表
	private Class<?>[] methodParameterTypes = new Class<?>[]{};

	public ServiceMethodHandler() {
    }

    public Object getHandler() {
        return handler;
    }

    public void setHandler(Object handler) {
        this.handler = handler;
    }

    public Method getHandlerMethod() {
        return handlerMethod;
    }

    public void setHandlerMethod(Method handlerMethod) {
        this.handlerMethod = handlerMethod;
    }

    public void setIgnoreSignFieldNames(Set<String> ignoreSignFieldNames) {
        this.ignoreSignFieldNames = ignoreSignFieldNames;
    }

    public Set<String> getIgnoreSignFieldNames() {
        return ignoreSignFieldNames;
    }

    public ServiceMethodDefinition getServiceMethodDefinition() {
        return serviceMethodDefinition;
    }

    public void setServiceMethodDefinition(ServiceMethodDefinition serviceMethodDefinition) {
        this.serviceMethodDefinition = serviceMethodDefinition;
    }

    public static String methodWithVersion(String methodName, String version) {
        return methodName + "#" + version;
    }

    public List<String> getUploadFileFieldNames() {
        return uploadFileFieldNames;
    }

    public void setUploadFileFieldNames(List<String> uploadFileFieldNames) {
        this.uploadFileFieldNames = uploadFileFieldNames;
    }

    public boolean hasUploadFiles(){
        return uploadFileFieldNames != null && uploadFileFieldNames.size() > 0;
    }

	public Class<?>[] getMethodParameterTypes() {
		return methodParameterTypes;
	}

	public void setMethodParameterTypes(Class<?>[] methodParameterTypes) {
		this.methodParameterTypes = methodParameterTypes;
	}
}

