package com.tagmob.client;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

public class AdFetcher {

    public static final String NO_ADVERTISEMENT = "no advertisement";

    public static final String TYPE = "adType:";

    public static final String TEXT = "text:";

    public static final String KEY = "key:";

    public static final String SIZE = "size:";

    public static final String IMAGE_SRC = "src:";
    
    public static final String CAMPAIGN_ID = "campaignId:";
    
    public static final String CPC = "cpc:";

    private static final URL TAG_MOB_REQUEST_URL;

    static {
        URL url;
        try {
            url = new URL(TagMobURLs.TAG_MOB_REQUEST_URL);
        } catch (MalformedURLException ignore) {
            throw new AssertionError("Malformed TagMob request URL");
        }
        TAG_MOB_REQUEST_URL = url;
    }

    private int timeoutMillis;

    private List<Header> headers;

    public AdFetcher(javax.servlet.http.HttpServletRequest javaxRequest, int timeoutMillis, List<? extends AdRequest> requests) {
        this.timeoutMillis = timeoutMillis;
        this.headers = new ArrayList<Header>();

        Enumeration<String> headerNames = javaxRequest.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            if (headerName == null || (headerName = headerName.trim()).isEmpty()) {
                continue;
            }
            Enumeration<String> headerValues = javaxRequest.getHeaders(headerName);
            while (headerValues.hasMoreElements()) {
                String headerValue = headerValues.nextElement();
                if (headerValue == null || (headerValue = headerValue.trim()).isEmpty()) {
                    continue;
                }
                
                headers.add(Header.create(headerName, headerValue));
            }
        }

        if (headers.isEmpty()) {
            throw new IllegalStateException("request has no headers");
        }
        
        headers.add(Header.create("tm-client-ip", javaxRequest.getRemoteAddr()));

        if (requests == null || requests.isEmpty()) {
            headers.add(Header.create("tm-request", new AnyAdRequest(3).toString()));
        } else {
            StringBuilder sb = new StringBuilder(headers.size() * 8);
            String limiter = "";
            for (AdRequest request : requests) {
                sb.append(limiter).append(request);
                limiter = ",";
            }
            headers.add(Header.create("tm-request", sb.toString()));
        }
    }

    public List<AdResponse> fetchAds() throws IOException {
        Response response = HttpConnection.connect(TAG_MOB_REQUEST_URL)
                .withTimeout(timeoutMillis).withData(headers).post();
        if (response.statusCode() == 200) {
            return parse(response.body());
        }
        return Collections.emptyList();
    }

    static List<AdResponse> parse(String responseBody) {
        
        if (NO_ADVERTISEMENT.equals(responseBody)) {
            return Collections.<AdResponse>emptyList();
        }
        
        List<AdResponse> adResponses = new ArrayList<AdResponse>();
        
        String[] lines = responseBody.split("\\r?\\n");
        
        String adType = null;
        ImageSize size = null;
        String imageSrc = null;
        String key = null;
        String text = null;
        int campaignId = -1;
        BigDecimal cpc = null;
                
        for (String line : lines) {
            if (line.isEmpty()) {
                if (AdType.IMAGE.toString().equals(adType)) {
                    adResponses.add(new AdResponse(key, AdType.IMAGE, null, size, imageSrc, campaignId, cpc));
                }
                else if (AdType.TEXT.toString().equals(adType)) {
                    adResponses.add(new AdResponse(key, AdType.TEXT, text, null, null, campaignId, cpc));
                }
                
                adType = null;
                size = null;
                imageSrc = null;
                key = null;
                text = null;
                campaignId = -1;
                cpc = null;
                
                continue;
            }
            
            int index = line.indexOf(":");
            if (index == -1) {
                throw new IllegalArgumentException("Illegal line: " + line);
            }
            
            String k = line.substring(0, index + 1).trim();
            String v = line.substring(index + 1);
            
            if (TYPE.equals(k)) {
                adType = v;
            }
            else if (SIZE.equals(k)) {
                size = ImageSize.from(v);
            }
            else if (IMAGE_SRC.equals(k)) {
                imageSrc = v;
            }
            else if (KEY.equals(k)) {
                key = v;
            }
            else if (TEXT.equals(k)) {
                text = v;
            }
            else if (CAMPAIGN_ID.equals(k)) {
                campaignId = Integer.parseInt(v.trim());
            }
            else if (CPC.equals(k)) {
                cpc = new BigDecimal(v.trim());
            }
        }
        
        if (adType != null) {
            if (AdType.IMAGE.toString().equals(adType)) {
                adResponses.add(new AdResponse(key, AdType.IMAGE, null, size, imageSrc, campaignId, cpc));
            }
            else if (AdType.TEXT.toString().equals(adType)) {
                adResponses.add(new AdResponse(key, AdType.TEXT, text, null, null, campaignId, cpc));
            }
        }
                
        return adResponses;
    }
}
