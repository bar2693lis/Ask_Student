package com.barlis.chat.Model;

// Request states
public enum ERequestStatus {
    REQUEST_AVAILABLE(1), REQUEST_TAKEN(2), REQUEST_DONE(3);

    private int value;
    ERequestStatus(int value) {
        this.value = value;
    }

    public int getValue() {return value;}
}
