package com.tagmob.client;

import static com.tagmob.client.Util.requireNonNull;


public class AdResponse {

    private final AdType type;

    private final String text;

    private final String key;

    private final ImageSize imgSize;
    
    private final String imgSrc;

    AdResponse(String key, AdType type, String text, ImageSize imgSize, String imgSrc) {
        this.type = requireNonNull(type, "type must not be null");
        this.text = type.equals(AdType.TEXT)
                ? requireNonNull(text, "text must not be null for text ads")
                : null;
        this.key = key;
        this.imgSize = imgSize;
        this.imgSrc = imgSrc;
    }

    public AdType type() {
        return type;
    }

    public String impressionUrl() {
        return TagMobURLs.IMPRESSION_URL + key;
    }

    public String clickUrl() {
        return TagMobURLs.CLICK_URL + key;
    }

    public String text() {
        return text;
    }

    public ImageSize imgSize() {
        if (type == null) {
            throw new IllegalStateException("Text ads don't have image size, check type first");
        }
        return imgSize;
    }

    @Override
    public String toString() {
        return "type:" + type + "\n" +
                "text:" + text + "\n" +
                "key:" + key + "\n" +
                "imgSize:" + imgSize == null ? null : imgSize.toString() + "\n" +
                "src" + imgSrc;
    }

    /**
     * @return the imgSrc
     */
    public String getImgSrc() {
        return imgSrc;
    }
}
