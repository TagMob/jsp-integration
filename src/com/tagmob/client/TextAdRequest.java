package com.tagmob.client;

public class TextAdRequest implements AdRequest {

    private final int adCount;

    public TextAdRequest(int adCount) {
        this.adCount = (adCount <= 0) ? 1 : adCount;
    }

    @Override
    public String toString() {
        return AdType.TEXT + "_" + adCount;
    }

    public static TextAdRequest from (String s) {
        //TEXT_2
        int index = s.indexOf("_");
        if (index == -1
                || !s.substring(0, index).equals(AdType.TEXT.toString())) {
            return null;
        }

        String countString = s.substring(index + 1);
        int count;
        try {
            count = Integer.parseInt(countString);
        }
        catch (NumberFormatException e) {
            return null;
        }

        return new TextAdRequest(count);
    }

}
