package com.barlis.chat.Model;

// Enum for all result codes in the app
public enum EResultCodes {
    SEARCH_USER(1) , OPEN_REQUEST(2), UPDATE_REQUEST_WORKER(3), REMOVE_WORKER(4), QUIT_REQUEST(5), CLOSE_REQUEST(6);

    private int resultCode;
    EResultCodes(int resultCode) { this.resultCode = resultCode;}
    public int getValue() {return resultCode;}
}
