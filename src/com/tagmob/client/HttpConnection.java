package com.tagmob.client;


import static com.tagmob.client.Util.requireNonNull;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;

public class HttpConnection {

    private Request request;

    private Response response;

    private HttpConnection() {
        request = new Request();
        response = new Response();
    }

    public static HttpConnection connect(URL url) {
        requireNonNull(url, "url must not be null");
        HttpConnection con = new HttpConnection();
        con.url(url);
        return con;
    }

    public Response get() throws IOException {
        request.method(Method.GET);
        execute();
        return response;
    }

    Response post() throws IOException {
        request.method(Method.POST);
        return execute();
    }

    private Response execute() throws IOException {
        response = Response.execute(request);
        return response;
    }

    HttpConnection withTimeout(int millis) {
        request.timeout(millis);
        return this;
    }

    HttpConnection withData(Collection<Header> data) {
        request.data(data);
        return this;
    }

    private HttpConnection url(URL url) {
        requireNonNull(url, "url must not be null");
        request.url(url);
        return this;
    }

    public void setHeader(String name, String value) {
        request.header(name, value);
    }

    public HttpConnection followRedirects(boolean follow) {
        request.followRedirects(follow);
        return this;
    }

}
