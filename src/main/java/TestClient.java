import java.nio.ByteBuffer;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.util.component.LifeCycle;

public class TestClient implements Runnable
{
    private final HttpClient client = new HttpClient();
    private final int numContent;

    public TestClient()
    {
        this.numContent = -1;
    }

    public TestClient(int numContent)
    {
        this.numContent = numContent;
    }

    public void stop() throws Exception
    {
        client.stop();
    }

    @Override
    public void run()
    {
        try
        {
            System.err.println("Testing from Jetty Client...");
            client.start();
            ContentResponse response = client.newRequest("http://localhost:8080").onResponseContent(new Response.ContentListener()
            {
                private int numReceived = 0;

                @Override
                public void onContent(Response response, ByteBuffer byteBuffer)
                {
                    numReceived += byteBuffer.remaining();
                    if (numContent > 0 && numReceived > numContent)
                        response.abort(new Throwable("Test is aborting."));
                }
            }).send();

            System.err.println(response);
        }
        catch (Throwable t)
        {
            System.err.println("Testing Client Failed " + t.toString());
        }
        finally
        {
            LifeCycle.stop(client);
        }
    }
}
