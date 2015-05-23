package com.tagmob.client;


public enum ImageSize {

    _120x20("120x20", true),
    _168x28("168x28", true),
    _216x36 ("216x36", true),
    _300x50("300x50", true),
    _320x50("320x50", true),
    _320x480("320x480", false),
    _300x250("300x250", false),
    _250x250("250x250", false),
    _468x60("468x60", false),
    _728x90("728x90", false),
    any("any", false);

    private final String size;

    private final boolean isMandatory;

    ImageSize(String size, boolean isMandatory) {
        this.size = size;
        this.isMandatory = isMandatory;
    }

    @Override
    public String toString() {
        return size;
    }

    public boolean isMandatory() {
        return isMandatory;
    }

    public static ImageSize from(String s) {
        for (ImageSize size : ImageSize.values()) {
            if (size.size.equals(s)) {
                return size;
            }
        }
        return valueOf(s);
    }
}
