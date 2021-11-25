import java.io.IOException;
import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import servlet.ServletContainer;
import servlet.impl.JettyContainer;
import servlet.impl.TomcatContainer;
import servlet.impl.UndertowContainer;

public class ContainerTester
{
    private static TestClient client;
    private ServletContainer container;

    @AfterEach
    public void after() throws Exception
    {
        if (container != null)
            container.stop();
    }

    @ValueSource(classes = {JettyContainer.class, TomcatContainer.class, UndertowContainer.class})
    @ParameterizedTest
    public void test(Class<? extends ServletContainer> containerClass) throws Exception
    {
        container = containerClass.getDeclaredConstructor().newInstance();
        container.addServlet(AsyncServlet.class, "/");
        container.start();

        client = new TestClient(1024 * 1024 * 128);
        client.run();
    }

    private static final String data = "the quick brown fox jumps over the lazy dog";
    public static class AsyncServlet extends HttpServlet
    {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException
        {
            AsyncContext asyncContext = req.startAsync();
            asyncContext.addListener(new AsyncListener()
            {
                @Override
                public void onComplete(AsyncEvent event) throws IOException
                {
                    System.err.println("## AsyncListener: onComplete() notification");
                }

                @Override
                public void onTimeout(AsyncEvent event) throws IOException
                {
                    System.err.println("## AsyncListener: onTimeout() notification");
                }

                @Override
                public void onError(AsyncEvent event) throws IOException
                {
                    System.err.println("## AsyncListener: onError() notification " + event.getThrowable());
                }

                @Override
                public void onStartAsync(AsyncEvent event) throws IOException
                {
                    System.err.println("## AsyncListener: onStartAsync() notification");
                }
            });

            ServletOutputStream outputStream = resp.getOutputStream();
            outputStream.setWriteListener(new WriteListener()
            {
                private int loopCount = 0;

                @Override
                public void onWritePossible() throws IOException
                {
                    try
                    {
                        while (outputStream.isReady())
                        {
                            outputStream.write(data.getBytes());
                            loopCount++;
                        }
                    }
                    catch (Throwable t)
                    {
                        System.err.println("## WriteListener: throwing from onWritePossible() after " + loopCount + " iteration");
                        System.err.println("## AsyncServlet: isReady() == " + outputStream.isReady());
                        t.printStackTrace();
                        // throw t;
                    }
                }

                @Override
                public void onError(Throwable t)
                {
                    System.err.println("## WriteListener: onError after " + loopCount + " iterations");
                    System.err.println("## WriteListener: isReady() == " + outputStream.isReady());
                    t.printStackTrace();
                    asyncContext.complete();
                }
            });
        }
    }

}
