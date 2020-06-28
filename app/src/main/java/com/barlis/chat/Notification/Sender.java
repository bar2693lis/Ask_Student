package com.barlis.chat.Notification;

public class Sender {

    public Data data;
    public String to;
    public String priority;

    public Sender(Data data, String to) {
        this.data = data;
        this.to = to;
        this.priority = "high";
    }
}
