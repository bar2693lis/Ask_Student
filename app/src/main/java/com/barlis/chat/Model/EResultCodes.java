package com.barlis.chat.Model;

public enum EResultCodes {
    SEARCH_USER(1) , OPEN_REQUEST(2), UPDATE_REQUEST_WORKER(3), REMOVE_WORKER(4), QUIT_REQUEST(5), CLOSE_REQUEST(6);

    private int resultCode;
    EResultCodes(int resultCode) { this.resultCode = resultCode;}
    public int getValue() {return resultCode;}
}
