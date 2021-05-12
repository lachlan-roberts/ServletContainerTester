package servlet;

import javax.servlet.Servlet;

public interface ServletContainer
{
    void start() throws Exception;

    void stop() throws Exception;

    void addServlet(Class<? extends Servlet> servlet, String pathSpec);
}
