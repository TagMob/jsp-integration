package com.tagmob.client;

import java.util.ArrayList;
import java.util.Collection;

import static com.tagmob.client.Util.isTrue;

public class Request extends Base<Request> {

    private int timeoutMilliseconds;

    private int maxBodySizeBytes;

    private boolean followRedirects;

    private Collection<Header> data;

    private boolean ignoreHttpErrors = false;

    private boolean ignoreContentType = false;

    Request() {
        timeoutMilliseconds = 3000;
        maxBodySizeBytes = 1024 * 1024; // 1 MB;
        followRedirects = true;
        data = new ArrayList<Header>();
        method = Method.GET;
        headers.put("Accept-Encoding", "gzip");
    }

    public int timeout() {
        return timeoutMilliseconds;
    }

    public Request timeout(int millis) {
        isTrue(millis >= 0, "Timeout milliseconds must be 0 (infinite) or greater");
        timeoutMilliseconds = millis;
        return this;
    }

    public int maxBodySize() {
        return maxBodySizeBytes;
    }

    public Request maxBodySize(int bytes) {
        isTrue(bytes >= 0, "maxSize must be 0 (unlimited) or larger");
        maxBodySizeBytes = bytes;
        return this;
    }

    public boolean followRedirects() {
        return followRedirects;
    }

    public Request followRedirects(boolean followRedirects) {
        this.followRedirects = followRedirects;
        return this;
    }

    public boolean ignoreHttpErrors() {
        return ignoreHttpErrors;
    }

    public Request ignoreHttpErrors(boolean ignoreHttpErrors) {
        this.ignoreHttpErrors = ignoreHttpErrors;
        return this;
    }

    public boolean ignoreContentType() {
        return ignoreContentType;
    }

    public Request ignoreContentType(boolean ignoreContentType) {
        this.ignoreContentType = ignoreContentType;
        return this;
    }

    public Request data(Collection<Header> headers) {
        if (headers == null || headers.isEmpty()) {
            throw new IllegalArgumentException("headers must not be null or empty");
        }
        this.data = headers;
        return this;
    }

    public Collection<Header> data() {
        return data;
    }

}
