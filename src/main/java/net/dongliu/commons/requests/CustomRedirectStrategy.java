package net.dongliu.commons.requests;

import net.dongliu.commons.lang.collection.Pair;
import org.apache.http.Header;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolException;
import org.apache.http.client.RedirectStrategy;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Dong Liu dongliu@live.cn
 */
public class CustomRedirectStrategy implements RedirectStrategy {

    private final DefaultRedirectStrategy strategy = new DefaultRedirectStrategy();
    private final Response<?> lastResponse;

    public CustomRedirectStrategy(Response<?> lastResponse) {
        this.lastResponse = lastResponse;
    }

    @Override
    public boolean isRedirected(HttpRequest request, HttpResponse response, HttpContext context)
            throws ProtocolException {
        return strategy.isRedirected(request, response, context);
    }

    @Override
    public HttpUriRequest getRedirect(HttpRequest request, HttpResponse response, HttpContext context)
            throws ProtocolException {
        Response<byte[]> resp = new Response<>();
        resp.statusCode(response.getStatusLine().getStatusCode());
        Header[] respHeaders = response.getAllHeaders();
        List<Pair<String, String>> headers = new ArrayList<>(respHeaders.length);
        for (org.apache.http.Header header : respHeaders) {
            headers.add(Pair.of(header.getName(), header.getValue()));
        }
        resp.headers(headers);
        try {
            resp.body(EntityUtils.toByteArray(response.getEntity()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        lastResponse.addHistory(resp);
        return strategy.getRedirect(request, response, context);
    }
}
