package rop.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.Assert;
import rop.Interceptor;
import rop.config.InterceptorHolder;
import rop.config.RopEventListenerHodler;
import rop.event.RopEventListener;
import rop.security.*;
import rop.session.SessionManager;

import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * <pre>
 *     Spring集成配置类
 * </pre>
 * @see rop.impl.AnnotationServletServiceRouter
 * @author 陈雄华
 * @author luopeng
 * @version 1.0
 */
public class AnnotationServletServiceRouterFactoryBean
        implements FactoryBean<AnnotationServletServiceRouter>,ApplicationContextAware, InitializingBean, DisposableBean{

    private static final String ALL_FILE_TYPES = "*";

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private ApplicationContext applicationContext;

    private ThreadPoolExecutor threadPoolExecutor;

	/**
	 * 事件广播线程池，主要处理事件广播
	 */
	private ThreadPoolExecutor eventPoolExecutor;

    private SessionManager sessionManager;

    private AppSecretManager appSecretManager;

    private ServiceAccessController serviceAccessController;

    private InvokeTimesController invokeTimesController;

    private boolean signEnable = true;

    private String extErrorBasename;

    private String[] extErrorBasenames;

    private int serviceTimeoutSeconds = -1;

	//时间戳误差容忍度
	private long timestampTolerance = -1;

    private AnnotationServletServiceRouter serviceRouter;

    //多值用逗号分隔,默认支持4种格式的文件
    private String uploadFileTypes = ALL_FILE_TYPES;

    //单位为K，默认为10M
    private int uploadFileMaxSize = 10 * 1024;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }


    @Override
    public void destroy() throws Exception {
        serviceRouter.shutdown();
    }

    @Override
    public Class<?> getObjectType() {
        return AnnotationServletServiceRouter.class;
    }

    @Override
    public AnnotationServletServiceRouter getObject() throws Exception {
        return this.serviceRouter;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    public void setInvokeTimesController(InvokeTimesController invokeTimesController) {
        this.invokeTimesController = invokeTimesController;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        //实例化一个AnnotationServletServiceRouter
        serviceRouter = new AnnotationServletServiceRouter();

        //设置国际化错误资源
        if (extErrorBasename != null) {
            serviceRouter.setExtErrorBasename(extErrorBasename);
        }

        if (extErrorBasenames != null) {
            serviceRouter.setExtErrorBasenames(extErrorBasenames);
        }

        DefaultSecurityManager securityManager = BeanUtils.instantiate(DefaultSecurityManager.class);

        securityManager.setSessionManager(sessionManager);
        securityManager.setAppSecretManager(appSecretManager);
        securityManager.setServiceAccessController(serviceAccessController);
        securityManager.setInvokeTimesController(invokeTimesController);
        securityManager.setFileUploadController(buildFileUploadController());

        serviceRouter.setSecurityManager(securityManager);
        serviceRouter.setThreadPoolExecutor(threadPoolExecutor);
		serviceRouter.setEventPoolExecutor(eventPoolExecutor);
        serviceRouter.setSignEnable(signEnable);
        serviceRouter.setServiceTimeoutSeconds(serviceTimeoutSeconds);
        serviceRouter.setSessionManager(sessionManager);
        serviceRouter.setInvokeTimesController(invokeTimesController);
		serviceRouter.setTimestampTolerance(timestampTolerance);

        //注册拦截器
        ArrayList<Interceptor> interceptors = getInterceptors();
        if (interceptors != null) {
            for (Interceptor interceptor : interceptors) {
                serviceRouter.addInterceptor(interceptor);
            }
            if (logger.isInfoEnabled()) {
                logger.info("共注册了" + interceptors.size() + "拦截器.");
            }
        }

        //注册监听器
        ArrayList<RopEventListener> listeners = getListeners();
        if (listeners != null) {
            for (RopEventListener listener : listeners) {
                serviceRouter.addListener(listener);
            }
            if (logger.isInfoEnabled()) {
                logger.info("共注册了" + listeners.size() + "事件监听器.");
            }
        }

        //设置Spring上下文信息
        serviceRouter.setApplicationContext(this.applicationContext);

        //启动之
        serviceRouter.startup();
    }

    private DefaultFileUploadController buildFileUploadController() {
        Assert.notNull(this.uploadFileTypes, "允许上传的文件类型不能为空");
        if(ALL_FILE_TYPES.equals(uploadFileTypes.trim())){
            return new DefaultFileUploadController(this.uploadFileMaxSize);
        }else {
            String[] items = this.uploadFileTypes.split(",");
            List<String> fileTypes = Arrays.asList(items);
            return new DefaultFileUploadController(fileTypes, this.uploadFileMaxSize);
        }
    }

    private ArrayList<Interceptor> getInterceptors() {
        Map<String, InterceptorHolder> interceptorMap = this.applicationContext.getBeansOfType(InterceptorHolder.class);
        if (interceptorMap != null && interceptorMap.size() > 0) {
            ArrayList<Interceptor> interceptors = new ArrayList<Interceptor>(interceptorMap.size());

            //从Spring容器中获取Interceptor
            for (InterceptorHolder interceptorHolder : interceptorMap.values()) {
                interceptors.add(interceptorHolder.getInterceptor());
            }

            //根据getOrder()值排序
            Collections.sort(interceptors, new Comparator<Interceptor>() {
                public int compare(Interceptor o1, Interceptor o2) {
                    if (o1.getOrder() > o2.getOrder()) {
                        return 1;
                    } else if (o1.getOrder() < o2.getOrder()) {
                        return -1;
                    } else {
                        return 0;
                    }
                }
            });
            return interceptors;
        } else {
            return null;
        }
    }

    private ArrayList<RopEventListener> getListeners() {
        Map<String, RopEventListenerHodler> listenerMap = this.applicationContext.getBeansOfType(RopEventListenerHodler.class);
        if (listenerMap != null && listenerMap.size() > 0) {
            ArrayList<RopEventListener> ropEventListeners = new ArrayList<RopEventListener>(listenerMap.size());

            //从Spring容器中获取Interceptor
            for (RopEventListenerHodler listenerHolder : listenerMap.values()) {
                ropEventListeners.add(listenerHolder.getRopEventListener());
            }
            return ropEventListeners;
        } else {
            return null;
        }
    }

    public void setThreadPoolExecutor(ThreadPoolExecutor threadPoolExecutor) {
        this.threadPoolExecutor = threadPoolExecutor;
    }

    public void setSignEnable(boolean signEnable) {
        this.signEnable = signEnable;
    }

    public void setExtErrorBasename(String extErrorBasename) {
        this.extErrorBasename = extErrorBasename;
    }

    public void setExtErrorBasenames(String[] extErrorBasenames) {
        this.extErrorBasenames = extErrorBasenames;
    }

    public void setServiceTimeoutSeconds(int serviceTimeoutSeconds) {
        this.serviceTimeoutSeconds = serviceTimeoutSeconds;
    }

    public void setSessionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    public void setAppSecretManager(AppSecretManager appSecretManager) {
        this.appSecretManager = appSecretManager;
    }

    public void setServiceAccessController(ServiceAccessController serviceAccessController) {
        this.serviceAccessController = serviceAccessController;
    }

    public String getUploadFileTypes() {
        return uploadFileTypes;
    }

    public void setUploadFileTypes(String uploadFileTypes) {
        this.uploadFileTypes = uploadFileTypes;
    }

    public int getUploadFileMaxSize() {
        return uploadFileMaxSize;
    }

    public void setUploadFileMaxSize(int uploadFileMaxSize) {
        this.uploadFileMaxSize = uploadFileMaxSize;
    }

	public long getTimestampTolerance() {
		return timestampTolerance;
	}

	public void setTimestampTolerance(long timestampTolerance) {
		this.timestampTolerance = timestampTolerance;
	}

	public ThreadPoolExecutor getEventPoolExecutor() {
		return eventPoolExecutor;
	}

	public void setEventPoolExecutor(ThreadPoolExecutor eventPoolExecutor) {
		this.eventPoolExecutor = eventPoolExecutor;
	}
}

