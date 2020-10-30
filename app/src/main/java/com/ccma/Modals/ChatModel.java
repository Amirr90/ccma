package com.ccma.Modals;

public class ChatModel {
    String comment;
    long timestamp;
    String email;
    String San_Amt;
    String San_Dt;
    String Address;

    public ChatModel(String comment, long timestamp, String email, String san_Amt, String san_Dt, String address) {
        this.comment = comment;
        this.timestamp = timestamp;
        this.email = email;
        San_Amt = san_Amt;
        San_Dt = san_Dt;
        Address = address;
    }

    public String getAddress() {
        return Address;
    }

    public String getSan_Amt() {
        return San_Amt;
    }

    public String getSan_Dt() {
        return San_Dt;
    }

    public String getEmail() {
        return email;
    }

    public String getComment() {
        return comment;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
