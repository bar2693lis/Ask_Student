package com.barlis.chat.Model;

// Enum for all request codes in the app
public enum ERequestCodes {
    LOCATION_PERMISSION(1) , NEW_REQUEST(2), UPDATE_REQUEST(3), READ_PERMISSION(4), WRITE_PREMISSION(5), GET_FILE(6);

    private int requestCode;
    ERequestCodes(int requestCode) { this.requestCode = requestCode;}
    public int getValue() {return requestCode;}
}
