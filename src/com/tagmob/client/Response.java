package com.tagmob.client;


import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import static com.tagmob.client.Util.encodeUrl;
import static com.tagmob.client.Util.isTrue;
import static com.tagmob.client.Util.requireNonNull;


public class Response extends Base<Response> {

    private static final int HTTP_TEMP_REDIR = 307; // http/1.1 temporary redirect, not in Java's set.

    private static final Pattern CHARSET_PATTERN = Pattern.compile("(?i)\\bcharset=\\s*(?:\"|')?([^\\s,;\"']*)");

    private static final String DEFAULT_CHARSET = "UTF-8"; // used if not found in header or meta charset

    private static final int BUFFER_SIZE = 4096;

    private static final int MAX_REDIRECTS = 20;

    private int statusCode;

    private String statusMessage;

    private ByteBuffer byteData;

    private String charset;

    private String contentType;

    private boolean executed = false;

    private int numRedirects = 0;

    private Request request;

    /*
     * For example {@code application/atom+xml;charset=utf-8}.
     * Stepping through it: start with {@code "application/"}, follow with word
     * characters up to a {@code "+xml"}, and then maybe more ({@code .*}).
     */
    private static final Pattern xmlContentTypeRxp = Pattern.compile("application/\\w+\\+xml.*");

    static Response execute(Request request) throws IOException {
        return execute(request, null);
    }

    Response() {}

    private Response(Response previousResponse) throws IOException {
        if (previousResponse != null) {
            numRedirects = previousResponse.numRedirects + 1;
            if (numRedirects >= MAX_REDIRECTS) {
                throw new IOException(String.format("Too many redirects occurred trying to load URL %s",
                        previousResponse.url()));
            }
        }
    }

    static Response execute(Request request, Response previousResponse) throws IOException {
        requireNonNull(request, "Request must not be null");
        String protocol = request.url().getProtocol();
        if (!protocol.equals("http") && !protocol.equals("https")) {
            throw new MalformedURLException(("Only http & https protocols are supported"));
        }

        if (request.method() == Method.GET && request.data().size() > 0) {
            serialiseRequestUrl(request); // appends query string
        }
        HttpURLConnection connection = createConnection(request);
        Response res;
        try {
            connection.connect();
            if (request.method() == Method.POST) {
                writePost(request.data(), connection.getOutputStream());
            }

            int status = connection.getResponseCode();
            boolean needsRedirect = false;
            if (status != HttpURLConnection.HTTP_OK) {
                if (status == HttpURLConnection.HTTP_MOVED_TEMP
                        || status == HttpURLConnection.HTTP_MOVED_PERM
                        || status == HttpURLConnection.HTTP_SEE_OTHER
                        || status == HTTP_TEMP_REDIR) {
                    needsRedirect = true;
                } else if (!request.ignoreHttpErrors()) {
//                    throw new HttpStatusException("HTTP error fetching URL", status, request.url().toString());
                }
            }
            res = new Response(previousResponse);
            res.setUpFromConnection(connection, previousResponse);
            if (needsRedirect && request.followRedirects()) {
                request.method(Method.GET); // always redirect with a get. any data param from original req are dropped.
                request.data().clear();

                String location = res.header("Location");
                // fix broken Location: http:/temp/AAG_New/en/index.php
                if (location != null && location.startsWith("http:/") && location.charAt(6) != '/') {
                    location = location.substring(6);
                }
                request.url(new URL(request.url(), encodeUrl(location)));
                // add response cookies to request (for e.g. login posts)
                for (Map.Entry<String, String> cookie : res.cookies.entrySet()) {
                    request.cookie(cookie.getKey(), cookie.getValue());
                }

                return execute(request, res);
            }
            res.request = request;

            String contentType = res.contentType();
            if (contentType != null
                    && !request.ignoreContentType()
                    && !contentType.startsWith("text/")
                    && !xmlContentTypeRxp.matcher(contentType).matches()
                    )
                ;
//                throw new UnsupportedMimeTypeException("Unhandled content type. Must be text/*, application/xml, or application/xhtml+xml",
//                        contentType, request.url().toString());

            InputStream bodyStream = null;
            InputStream dataStream = null;
            try {
                dataStream = connection.getErrorStream() != null ? connection.getErrorStream() : connection.getInputStream();
                bodyStream = res.hasHeader("Content-Encoding") && res.header("Content-Encoding").equalsIgnoreCase("gzip") ?
                        new BufferedInputStream(new GZIPInputStream(dataStream)) :
                        new BufferedInputStream(dataStream);
                res.byteData = readToByteBuffer(bodyStream, request.maxBodySize());
                res.charset = getCharsetFromContentType(res.contentType); // may be null, readInputStream deals with it
            } finally {
                if (bodyStream != null) bodyStream.close();
                if (dataStream != null) dataStream.close();
            }
        } finally {
            // per Java's documentation, this is not necessary, and precludes keepalives. However in practise,
            // connection errors will not be released quickly enough and can cause a too many open files error.
            connection.disconnect();
        }
        res.executed = true;
        return res;
    }

    private static ByteBuffer readToByteBuffer(InputStream inputStream, int maxSize) throws IOException {
        isTrue(maxSize >= 0, "maxSize must be 0 (unlimited) or larger");
        final boolean capped = maxSize > 0;
        byte[] buffer =  new byte[BUFFER_SIZE];
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(BUFFER_SIZE);
        int read;
        int remaining = maxSize;

        while (true) {
            read = inputStream.read(buffer);
            if (read == -1) {
                break;
            }
            if (capped) {
                if (read > remaining) {
                    outputStream.write(buffer, 0, remaining);
                    break;
                }
                remaining -= read;
            }
            outputStream.write(buffer, 0, read);
        }
        return ByteBuffer.wrap(outputStream.toByteArray());
    }

    private static String getCharsetFromContentType(String contentType) {
        if (contentType == null) {
            return null;
        }

        Matcher m = CHARSET_PATTERN.matcher(contentType);
        if (m.find()) {
            String charset = m.group(1).trim();
            charset = charset.replace("charset=", "");
            if (charset.length() == 0) {
                return null;
            }
            try {
                if (Charset.isSupported(charset)) {
                    return charset;
                }
                charset = charset.toUpperCase(Locale.ENGLISH);
                if (Charset.isSupported(charset)) {
                    return charset;
                }
            } catch (IllegalCharsetNameException e) {
                // if our advanced charset matching fails.... we just take the default
                return null;
            }
        }
        return null;
    }

    private static void serialiseRequestUrl(Request request) throws IOException {
        URL in = request.url();
        StringBuilder url = new StringBuilder();
        boolean first = true;

        url.append(in.getProtocol())
                .append("://")
                .append(in.getAuthority())
                .append(in.getPath())
                .append('?');
        if (in.getQuery() != null) {
            url.append(in.getQuery());
            first = false;
        }
        for (Header header : request.data()) {
            if (!first) {
                url.append('&');
            } else {
                first = false;
            }
            url.append(URLEncoder.encode(header.name(), DEFAULT_CHARSET))
                    .append('=')
                    .append(URLEncoder.encode(header.value(), DEFAULT_CHARSET));
        }
        request.url(new URL(url.toString()));
        request.data().clear();

    }

    private static void writePost(Collection<Header> data, OutputStream outputStream) throws IOException {
        OutputStreamWriter w = new OutputStreamWriter(outputStream, DEFAULT_CHARSET);
        boolean first = true;
        for (Header header : data) {
            if (!first) {
                w.append('&');
            } else {
                first = false;
            }

            w.write(URLEncoder.encode(header.name(), DEFAULT_CHARSET));
            w.write('=');
            w.write(URLEncoder.encode(header.value(), DEFAULT_CHARSET));
        }
        w.close();
    }

    private static HttpURLConnection createConnection(Request request) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) request.url().openConnection();
        connection.setRequestMethod(request.method().name());
        connection.setInstanceFollowRedirects(false); // don't rely on native redirection support
        connection.setConnectTimeout(request.timeout());
        connection.setReadTimeout(request.timeout());
        if (request.method() == Method.POST) {
            connection.setDoOutput(true);
        }
        for(Map.Entry<String, String> header : request.headers().entrySet()) {
            connection.addRequestProperty(header.getKey(), header.getValue());
        }
        return connection;
    }

    private void setUpFromConnection(HttpURLConnection connection, Response previousResponse)
            throws IOException {
        method = Method.valueOf(connection.getRequestMethod());
        url = connection.getURL();
        statusCode = connection.getResponseCode();
        statusMessage = connection.getResponseMessage();
        contentType = connection.getContentType();
        Map<String, List<String>> resHeaders = connection.getHeaderFields();
        processResponseHeaders(resHeaders);

        // if from a redirect, map previous response cookies into this response
        if (previousResponse != null) {

        }
    }

    void processResponseHeaders(Map<String, List<String>> resHeaders) {
        for (Map.Entry<String,List<String>> entry : resHeaders.entrySet()) {
            String name = entry.getKey();
            if (name == null) {
                continue; // http/1.1 line
            }

            List<String> values = entry.getValue();
            if (name.equalsIgnoreCase("Set-Cookie")) {

            } else {
                if (!values.isEmpty()) {
                    header(name, values.get(0));
                }
            }
        }
    }

    public int statusCode() {
        return statusCode;
    }

    public String statusMessage() {
        return statusMessage;
    }

    public String charset() {
        return charset;
    }

    public String contentType() {
        return contentType;
    }

    public String body() {
        String body;
        if (charset == null) {
            body = Charset.forName(DEFAULT_CHARSET).decode(byteData).toString();
        } else {
            body = Charset.forName(charset).decode(byteData).toString();
        }
        byteData.rewind();
        return body;
    }

}
