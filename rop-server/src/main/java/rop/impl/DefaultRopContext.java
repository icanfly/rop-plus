package rop.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;
import rop.*;
import rop.annotation.*;
import rop.request.RequestUtils;
import rop.request.ServiceRequest;
import rop.request.UploadFile;
import rop.session.SessionManager;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

/**
 * <pre>
 *     ROP框架的上下文
 * </pre>
 *
 * @author 陈雄华
 * @author luopeng
 * @version 1.0
 */
public class DefaultRopContext implements RopContext {

    protected static Logger logger = LoggerFactory.getLogger(DefaultRopContext.class);

    private final Map<String, ServiceMethodHandler> serviceHandlerMap = new HashMap<String, ServiceMethodHandler>();

    private final Set<String> serviceMethods = new HashSet<String>();

    private boolean signEnable;

    private SessionManager sessionManager;

	private long timestampTolerance;

    public DefaultRopContext(ApplicationContext context) {
        registerFromContext(context);
    }

    @Override
    public void addServiceMethod(String methodName, String version, ServiceMethodHandler serviceMethodHandler) {
        serviceMethods.add(methodName);
        serviceHandlerMap.put(ServiceMethodHandler.methodWithVersion(methodName, version), serviceMethodHandler);
    }

    @Override
    public ServiceMethodHandler getServiceMethodHandler(String methodName, String version) {
        return serviceHandlerMap.get(ServiceMethodHandler.methodWithVersion(methodName, version));
    }


    @Override
    public boolean isValidMethod(String methodName) {
        return serviceMethods.contains(methodName);
    }

    @Override
    public boolean isValidMethodVersion(String methodName, String version) {
        return serviceHandlerMap.containsKey(ServiceMethodHandler.methodWithVersion(methodName, version));
    }

    @Override
    public Map<String, ServiceMethodHandler> getAllServiceMethodHandlers() {
        return serviceHandlerMap;
    }

    @Override
    public boolean isSignEnable() {
        return signEnable;
    }

    @Override
    public SessionManager getSessionManager() {
        return this.sessionManager;
    }

	@Override
	public long getTimestampTolerance() {
		return timestampTolerance;
	}

	public void setTimestampTolerance(long timestampTolerance){
		this.timestampTolerance = timestampTolerance;
	}

	public void setSessionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    public void setSignEnable(boolean signEnable) {
        this.signEnable = signEnable;
    }

    /**
     * 扫描Spring容器中的Bean，查找有标注{@link rop.annotation.ServiceMethod}注解的服务方法，将它们注册到{@link RopContext}中缓存起来。
     *
     * @throws org.springframework.beans.BeansException
     *
     */
    private void registerFromContext(final ApplicationContext context) throws BeansException {
        if (logger.isDebugEnabled()) {
            logger.debug("对Spring上下文中的Bean进行扫描，查找ROP服务方法: " + context);
        }
        String[] beanNames = context.getBeanNamesForType(Object.class);
        for (final String beanName : beanNames) {
            Class<?> handlerType = context.getType(beanName);
            //只对标注 ServiceMethodBean的Bean进行扫描
            if(AnnotationUtils.findAnnotation(handlerType,ServiceMethodBean.class) != null){
                ReflectionUtils.doWithMethods(handlerType, new ReflectionUtils.MethodCallback() {
                            public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {
                                ReflectionUtils.makeAccessible(method);

                                ServiceMethod serviceMethod = AnnotationUtils.findAnnotation(method,ServiceMethod.class);
                                ServiceMethodBean serviceMethodBean =AnnotationUtils.findAnnotation(method.getDeclaringClass(),ServiceMethodBean.class);

                                ServiceMethodDefinition definition = null;
                                if (serviceMethodBean != null) {
                                    definition = buildServiceMethodDefinition(serviceMethodBean, serviceMethod);
                                } else {
                                    definition = buildServiceMethodDefinition(serviceMethod);
                                }

								//设置服务方法定义的参数注解 add by luopeng 2014.04.24
								definition.setMethodParameterAnnotaions(method.getParameterAnnotations());

                                ServiceMethodHandler serviceMethodHandler = new ServiceMethodHandler();
                                serviceMethodHandler.setServiceMethodDefinition(definition);

                                //1.set handler
                                serviceMethodHandler.setHandler(context.getBean(beanName)); //handler
                                serviceMethodHandler.setHandlerMethod(method); //handler'method

								List<Class<?>> methodParameterTypes = new ArrayList<Class<?>>();
								Class<?>[] parameterTypes = method.getParameterTypes();
								for(Class<?> type : parameterTypes){
									if(!ClassUtils.isAssignable(RopRequestContext.class, type) && !ClassUtils.isAssignable(ServiceRequest.class, type)){
										throw new RopException(method.getDeclaringClass().getName() + "." + method.getName()
															   + "入参类型非法:" + type.getName());
									}
								}

								serviceMethodHandler.setMethodParameterTypes(parameterTypes);

								List<Class<?>> classTypeList = Arrays.asList(parameterTypes);
                                //2.set sign fieldNames
                                serviceMethodHandler.setIgnoreSignFieldNames(RequestUtils.getIgnoreSignFieldNames(classTypeList));

                                //3.set fileItemFieldNames
                                serviceMethodHandler.setUploadFileFieldNames(getFileItemFieldNames(classTypeList));

                                addServiceMethod(definition.getMethod(), definition.getVersion(), serviceMethodHandler);

                                if (logger.isDebugEnabled()) {
                                    logger.debug("注册服务方法：" + method.getDeclaringClass().getCanonicalName() +
                                            "#" + method.getName() + "(..)");
                                }
                            }
                        },
                        new ReflectionUtils.MethodFilter() {
                            public boolean matches(Method method) {
                                return !method.isSynthetic() && AnnotationUtils.findAnnotation(method, ServiceMethod.class) != null;
                            }
                        }
                );
            }
        }
        if (context.getParent() != null) {
            registerFromContext(context.getParent());
        }
        if (logger.isInfoEnabled()) {
            logger.info("共注册了" + serviceHandlerMap.size() + "个服务方法");
        }
    }

    private ServiceMethodDefinition buildServiceMethodDefinition(ServiceMethod serviceMethod) {
        ServiceMethodDefinition definition = new ServiceMethodDefinition();
        definition.setMethod(serviceMethod.method());
        definition.setMethodTitle(serviceMethod.title());
        definition.setMethodGroup(serviceMethod.group());
        definition.setMethodGroupTitle(serviceMethod.groupTitle());
        definition.setTags(serviceMethod.tags());
        definition.setTimeout(serviceMethod.timeout());
        definition.setIgnoreSign(IgnoreSignType.isIgnoreSign(serviceMethod.ignoreSign()));
        definition.setVersion(serviceMethod.version());
        definition.setNeedInSession(NeedInSessionType.isNeedInSession(serviceMethod.needInSession()));
        definition.setObsoleted(ObsoletedType.isObsoleted(serviceMethod.obsoleted()));
        definition.setHttpAction(serviceMethod.httpAction());
        return definition;
    }

    private ServiceMethodDefinition buildServiceMethodDefinition(ServiceMethodBean serviceMethodBean, ServiceMethod serviceMethod) {
        ServiceMethodDefinition definition = new ServiceMethodDefinition();
        definition.setMethodGroup(serviceMethodBean.group());
        definition.setMethodGroupTitle(serviceMethodBean.groupTitle());
        definition.setTags(serviceMethodBean.tags());
        definition.setTimeout(serviceMethodBean.timeout());
        definition.setIgnoreSign(IgnoreSignType.isIgnoreSign(serviceMethodBean.ignoreSign()));
        definition.setVersion(serviceMethodBean.version());
        definition.setNeedInSession(NeedInSessionType.isNeedInSession(serviceMethodBean.needInSession()));
        definition.setHttpAction(serviceMethodBean.httpAction());
        definition.setObsoleted(ObsoletedType.isObsoleted(serviceMethodBean.obsoleted()));

        //如果ServiceMethod所提供的值和ServiceMethodGroup不一样，覆盖之
        definition.setMethod(serviceMethod.method());
        definition.setMethodTitle(serviceMethod.title());

        if (!ServiceMethodDefinition.DEFAULT_GROUP.equals(serviceMethod.group())) {
            definition.setMethodGroup(serviceMethod.group());
        }

        if (!ServiceMethodDefinition.DEFAULT_GROUP_TITLE.equals(serviceMethod.groupTitle())) {
            definition.setMethodGroupTitle(serviceMethod.groupTitle());
        }

        if (serviceMethod.tags() != null && serviceMethod.tags().length > 0) {
            definition.setTags(serviceMethod.tags());
        }

        if (serviceMethod.timeout() > 0) {
            definition.setTimeout(serviceMethod.timeout());
        }

        if (serviceMethod.ignoreSign() != IgnoreSignType.DEFAULT) {
            definition.setIgnoreSign(IgnoreSignType.isIgnoreSign(serviceMethod.ignoreSign()));
        }

        if (StringUtils.hasText(serviceMethod.version())) {
            definition.setVersion(serviceMethod.version());
        }

        if (serviceMethod.needInSession() != NeedInSessionType.DEFAULT) {
            definition.setNeedInSession(NeedInSessionType.isNeedInSession(serviceMethod.needInSession()));
        }

        if (serviceMethod.obsoleted() != ObsoletedType.DEFAULT) {
            definition.setObsoleted(ObsoletedType.isObsoleted(serviceMethod.obsoleted()));
        }

        if (serviceMethod.httpAction().length > 0) {
            definition.setHttpAction(serviceMethod.httpAction());
        }

        return definition;
    }

    private List<String> getFileItemFieldNames(List<Class<?>> classTypes) {
        final ArrayList<String> fileItemFieldNames = new ArrayList<String>(1);
		for(Class<?> classType : classTypes){
			if (classType != null) {
				if (logger.isDebugEnabled()) {
					logger.debug("获取" + classType.getCanonicalName() + "类型为FileItem的字段名");
				}

				ReflectionUtils.doWithFields(classType, new ReflectionUtils.FieldCallback() {
							public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
								fileItemFieldNames.add(field.getName());
							}
						},
						new ReflectionUtils.FieldFilter() {
							public boolean matches(Field field) {
								return ClassUtils.isAssignable(UploadFile.class, field.getType());
							}
						}
				);

			}
		}

        return fileItemFieldNames;
    }


}

