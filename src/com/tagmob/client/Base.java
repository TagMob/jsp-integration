package com.tagmob.client;

import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.tagmob.client.Util.checkNotEmpty;
import static com.tagmob.client.Util.requireNonNull;

abstract class Base<T> {

    URL url;

    Method method;

    Map<String, String> headers;

    Map<String, String> cookies;

    Base() {
        headers = new LinkedHashMap<String, String>();
        cookies = new LinkedHashMap<String, String>();
    }

    public URL url() {
        return url;
    }

    T url(URL url) {
        this.url = requireNonNull(url, "URL must be not null");
        return (T) this;
    }

    public Method method() {
        return method;
    }

    T method(Method method) {
        this.method = requireNonNull(method, "Method must not be null");
        return (T) this;
    }

    public Map<String, String> headers() {
        return headers;
    }

    public String header(String name) {
        return getHeaderCaseInsensitive(requireNonNull(name, "Header name must not be null"));
    }

    public T header(String name, String value) {
        checkNotEmpty(name, "Header name must not be empty");
        requireNonNull(value, "Header value must not be null");
        removeHeader(name); // ensures we don't get an "accept-encoding" and a "Accept-Encoding"
        headers.put(name, value);
        return (T) this;
    }

    public boolean hasHeader(String name) {
        checkNotEmpty(name, "Header name must not be empty");
        return getHeaderCaseInsensitive(name) != null;
    }

    public T removeHeader(String name) {
        checkNotEmpty(name, "Header name must not be empty");
        Map.Entry<String, String> entry = scanHeaders(name); // remove is case insensitive too
        if (entry != null)
            headers.remove(entry.getKey()); // ensures correct case
        return (T) this;
    }

    private String getHeaderCaseInsensitive(String name) {
        requireNonNull(name, "Header name must not be null");
        // quick evals for common case of title case, lower case, then scan for mixed
        String value = headers.get(name);
        if (value == null)
            value = headers.get(name.toLowerCase());
        if (value == null) {
            Map.Entry<String, String> entry = scanHeaders(name);
            if (entry != null)
                value = entry.getValue();
        }
        return value;
    }

    private Map.Entry<String, String> scanHeaders(String name) {
        String lc = name.toLowerCase();
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            if (entry.getKey().toLowerCase().equals(lc))
                return entry;
        }
        return null;
    }

    public String cookie(String name) {
        requireNonNull(name, "Cookie name must not be null");
        return cookies.get(name);
    }

    public T cookie(String name, String value) {
        checkNotEmpty(name, "Cookie name must not be empty");
        requireNonNull(value, "Cookie value must not be null");
        cookies.put(name, value);
        return (T) this;
    }

    public boolean hasCookie(String name) {
        checkNotEmpty(name, "Cookie name must not be empty");
        return cookies.containsKey(name);
    }

    public T removeCookie(String name) {
        checkNotEmpty(name, "Cookie name must not be empty");
        cookies.remove(name);
        return (T) this;
    }

    public Map<String, String> cookies() {
        return cookies;
    }
}
