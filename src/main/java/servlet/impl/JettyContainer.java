package servlet.impl;

import javax.servlet.Servlet;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import servlet.ServletContainer;

public class JettyContainer implements ServletContainer
{
    private final Server server;

    public JettyContainer()
    {
        server = new Server();
        ServerConnector serverConnector = new ServerConnector(server);
        serverConnector.setPort(8080);
        server.addConnector(serverConnector);
    }

    @Override
    public void start() throws Exception
    {
        server.start();
    }

    @Override
    public void stop() throws Exception
    {
        server.stop();
    }

    @Override
    public void addServlet(Class<? extends Servlet> servlet, String pathSpec)
    {
        ServletContextHandler contextHandler = new ServletContextHandler();
        contextHandler.setContextPath("/");
        contextHandler.addServlet(servlet, pathSpec);
        server.setHandler(contextHandler);
    }
}
