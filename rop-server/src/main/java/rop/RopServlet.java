package rop;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.WebApplicationContext;
import rop.security.AppSecretManager;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * <pre>
 *
 * HttpServlet是Rop框架的总入口，提供了多个定制ROP框架的配置参数：
 * 1.Rop会自己扫描Spring容器并加载之{@link AppSecretManager}及{@link rop.Interceptor}的Bean。
 *
 * 2.可通过"errorResourceBaseName"指定错误资源文件的基名，默认为“i18n/rop/ropError”.
 *
 * @author 陈雄华
 * @author luopeng
 * </pre>
 */
public class RopServlet extends HttpServlet {

    protected  Logger logger = LoggerFactory.getLogger(getClass());

    private ServiceRouter serviceRouter;


    /**
     * 将请求导向到Rop的框架中。
     *
     * @param req
     * @param resp
     * @throws javax.servlet.ServletException
     * @throws java.io.IOException
     */
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        serviceRouter.service(req, resp);
    }

    @Override
    public void init(ServletConfig servletConfig) throws ServletException {
        ApplicationContext ctx = getApplicationContext(servletConfig);
        this.serviceRouter = ctx.getBean(ServiceRouter.class);
        if (this.serviceRouter == null) {
            logger.error("在Spring容器中未找到" + ServiceRouter.class.getName() +
                    "的Bean,请在Spring配置文件中通过<aop:annotation-driven/>安装rop框架。");
        }
    }

    private ApplicationContext getApplicationContext(ServletConfig servletConfig) {
        return (ApplicationContext) servletConfig.getServletContext().getAttribute(
                WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
    }
}

