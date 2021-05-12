import java.io.IOException;
import javax.servlet.AsyncContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import servlet.JettyContainer;
import servlet.ServletContainer;
import servlet.TomcatContainer;
import servlet.UndertowContainer;

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

        client = new TestClient(1024);
        client.run();
    }

    private static final String data = "the quick brown fox jumps over the lazy dog";
    public static class AsyncServlet extends HttpServlet
    {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException
        {
            AsyncContext asyncContext = req.startAsync();
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
                            System.err.println("Write: " + loopCount);
                            outputStream.write(data.getBytes());
                            loopCount++;
                        }
                    }
                    catch (Throwable t)
                    {
                        System.err.println("throwing from onWritePossible() " + t.toString());
                        t.printStackTrace();
                        return;
                    }
                }

                @Override
                public void onError(Throwable t)
                {
                    System.err.println("onError after " + loopCount + " iterations");
                    t.printStackTrace();
                    new Throwable().printStackTrace();
                    asyncContext.complete();
                }
            });
        }
    }

}
