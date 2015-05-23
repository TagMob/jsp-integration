package com.tagmob.client;

import java.util.ArrayList;
import java.util.List;

public class AdRequestParser {

    public static List<AdRequest> from(String tmRequest) {
        List<AdRequest> adRequests = new ArrayList<AdRequest>();
        String[] adStrings = tmRequest.split(",");
        for (String adString : adStrings) {
            String trimmed = adString.trim();

            if (trimmed.startsWith(AdType.IMAGE.toString())) {
                ImageAdRequest imageAdRequest = ImageAdRequest.from(trimmed);
                if (imageAdRequest != null) {
                    adRequests.add(imageAdRequest);
                }
            }
            else if (trimmed.startsWith(AdType.TEXT.toString())) {
                TextAdRequest textAdRequest = TextAdRequest.from(trimmed);
                if (textAdRequest != null) {
                    adRequests.add(textAdRequest);
                }
            }
            else if (trimmed.startsWith(AdType.ANY.toString())) {
                AnyAdRequest anyAdRequest = AnyAdRequest.from(trimmed);
                if (anyAdRequest != null) {
                    adRequests.add(anyAdRequest);
                }
            }
        }

        return adRequests;
    }

}
