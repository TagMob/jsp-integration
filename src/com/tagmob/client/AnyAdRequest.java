package com.tagmob.client;

public class AnyAdRequest implements AdRequest {

    private final int adCount;

    public AnyAdRequest(int adCount) {
        this.adCount = (adCount <= 0) ? 1 : adCount;
    }

    @Override
    public String toString() {
        return AdType.ANY + "_" + adCount;
    }

    public static AnyAdRequest from(String s) {
        //ANY_2
        int index = s.indexOf("_");
        if (index == -1
                || !s.substring(0, index).equals(AdType.ANY.toString())) {
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

        return new AnyAdRequest(count);
    }

}
