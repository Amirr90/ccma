package com.ccma.Modals;

public class ChatModel2 {
    private String comment;
    private long timestamp;
    private String from;

    public ChatModel2(String comment, long timestamp, String from) {
        this.comment = comment;
        this.timestamp = timestamp;
        this.from = from;
    }

    public String getComment() {
        return comment;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getFrom() {
        return from;
    }
}
