package com.tagmob.client;

public class Header {

    private final String name;

    private final String value;

    public static Header create(String name, String value) {
        return new Header(name, value);
    }

    private Header(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String name() {
        return name;
    }

    public String value() {
        return value;
    }

    @Override
    public String toString() {
        return name + ": " + value;
    }

}
