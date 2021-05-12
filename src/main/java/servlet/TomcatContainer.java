package servlet;

import java.io.File;
import javax.servlet.Servlet;

import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;

public class TomcatContainer implements ServletContainer
{
    private final Tomcat tomcat;

    public TomcatContainer()
    {
        tomcat = new Tomcat();
        tomcat.setPort(8080);
        tomcat.getConnector();
    }

    @Override
    public void start() throws Exception
    {
        tomcat.start();
    }

    @Override
    public void stop() throws Exception
    {
        tomcat.stop();
        tomcat.destroy();
    }

    @Override
    public void addServlet(Class<? extends Servlet> servlet, String pathSpec)
    {
        Context ctx = tomcat.addContext("", new File(".").getAbsolutePath());
        String servletName = servlet.getName() + "-" + Integer.toHexString(this.hashCode());
        Tomcat.addServlet(ctx, servletName, servlet.getName()).setAsyncSupported(true);
        ctx.addServletMappingDecoded(pathSpec, servletName);
    }
}
