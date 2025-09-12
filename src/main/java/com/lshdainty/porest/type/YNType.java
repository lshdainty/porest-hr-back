package com.lshdainty.porest.type;

public enum YNType {
    Y("y", "Y", true),
    N("n", "N", false);

    private String lower;
    private String upper;
    private boolean bool;

    YNType(String lower, String upper,  boolean bool) {
        this.lower = lower;
        this.upper = upper;
        this.bool = bool;
    }
}
