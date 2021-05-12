package servlet;

import javax.servlet.Servlet;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.PathHandler;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;

import static io.undertow.servlet.Servlets.defaultContainer;
import static io.undertow.servlet.Servlets.deployment;
import static io.undertow.servlet.Servlets.servlet;

public class UndertowContainer implements ServletContainer
{
    private Undertow server;
    private DeploymentManager manager;

    @Override
    public void start() throws Exception
    {
        HttpHandler servletHandler = manager.start();
        PathHandler path = Handlers.path(Handlers.redirect("/"))
            .addPrefixPath("/", servletHandler);
        server = Undertow.builder()
            .addHttpListener(8080, "localhost")
            .setHandler(path)
            .build();
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
        DeploymentInfo servletBuilder = deployment()
            .setClassLoader(UndertowContainer.class.getClassLoader())
            .setContextPath("/")
            .setDeploymentName("test.war")
            .addServlets(servlet(servlet).addMapping(pathSpec).setAsyncSupported(true));
        manager = defaultContainer().addDeployment(servletBuilder);
        manager.deploy();
    }
}
