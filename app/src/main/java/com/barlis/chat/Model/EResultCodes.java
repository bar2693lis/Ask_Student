package com.barlis.chat.Model;

public enum EResultCodes {
    SEARCH_USER(1) , OPEN_REQUEST(2), UPDATE_REQUEST(3);

    private int resultCode;
    EResultCodes(int resultCode) { this.resultCode = resultCode;}
    public int getValue() {return resultCode;}
}
