import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.util.component.LifeCycle;

public class TestClient implements Runnable
{
    private final HttpClient client = new HttpClient();
    private final int maxContent;

    public TestClient()
    {
        this(-1);
    }

    public TestClient(int maxContent)
    {
        this.maxContent = maxContent;
    }

    @Override
    public void run()
    {
        try
        {
            CountDownLatch complete = new CountDownLatch(1);
            Response.Listener.Adapter adapter = new Response.Listener.Adapter()
            {
                private final AtomicInteger numReceived = new AtomicInteger();

                @Override
                public void onContent(Response response, ByteBuffer content)
                {
                    int numRcv = numReceived.addAndGet(content.remaining());
                    if (maxContent > 0 && numRcv > maxContent)
                        response.abort(new Throwable("Intentional Abort"));
                }

                @Override
                public void onComplete(Result result)
                {
                    System.err.println("Testing Client Complete " + result.getResponse());
                    complete.countDown();
                }
            };

            client.start();
            client.newRequest("http://localhost:8080").send(adapter);
            complete.await();
        }
        catch (Throwable t)
        {
            System.err.println("Testing Client Failed " + t);
            t.printStackTrace();
        }
        finally
        {
            LifeCycle.stop(client);
        }
    }
}
