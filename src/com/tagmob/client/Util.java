package com.tagmob.client;

class Util {

    static void checkNotEmpty(String string, String msg) {
        if (string == null || string.length() == 0)
            throw new IllegalArgumentException(msg);
    }

    static String encodeUrl(String url) {
        if (url == null) {
            return null;
        }
        return url.replaceAll(" ", "%20");
    }

    static void isTrue(boolean val, String msg) {
        if (!val) {
            throw new IllegalArgumentException(msg);
        }
    }
    
    public static <T> T requireNonNull(T obj, String message) {
        if (obj == null)
            throw new NullPointerException(message);
        return obj;
    }

}
