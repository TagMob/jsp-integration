package com.tagmob.client;

public class ImageAdRequest implements AdRequest {

    private final int adCount;

    private final ImageSize size;

    public ImageAdRequest(int adCount) {
        this(adCount, ImageSize.any);
    }

    public ImageAdRequest(int adCount, ImageSize size) {
        if (size == null) {
            throw new NullPointerException("use ImageSize.any to request any size");
        }
        this.size = size;
        this.adCount = (adCount <= 0) ? 1 : adCount;
    }

    @Override
    public String toString() {
        return AdType.IMAGE + "_" + adCount + "_" + size;
    }

    public static ImageAdRequest from(String s) {
        //IMAGE_2, IMAGE_2_250x250

        String[] tokens = s.split("_");
        if (tokens.length > 3 || tokens.length < 2) {
            return null;
        }

        if (!tokens[0].equals(AdType.IMAGE.toString())) {
            return null;
        }

        String countString = tokens[1];
        int count;
        try {
            count = Integer.parseInt(countString);
        }
        catch (NumberFormatException e) {
            return null;
        }

        if (tokens.length == 3) {
            ImageSize imageSize = null;
            for (ImageSize size : ImageSize.values()) {
                if (tokens[2].equals(size.toString())) {
                    imageSize = size;
                    break;
                }
            }

            if (imageSize == null) {
                return null;
            }

            return new ImageAdRequest(count, imageSize);
        }
        else {
            return new ImageAdRequest(count);
        }

    }
}
