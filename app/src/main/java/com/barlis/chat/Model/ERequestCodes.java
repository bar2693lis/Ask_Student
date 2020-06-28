package com.barlis.chat.Model;

// Enum for all request codes in the app
public enum ERequestCodes {
    LOCATION_PERMISSION(1) , NEW_REQUEST(2), UPDATE_REQUEST(3);

    private int requestCode;
    ERequestCodes(int requestCode) { this.requestCode = requestCode;}
    public int getValue() {return requestCode;}
}
