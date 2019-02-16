package com.blackviking.hosh.Model;

public class MessageModel {

    private String id;
    private String message;
    private String messageThumb;
    private String timeStamp;
    private String read;
    private String type;
    private String from;

    public MessageModel() {
    }

    public MessageModel(String id, String message, String messageThumb, String timeStamp, String read, String type, String from) {
        this.id = id;
        this.message = message;
        this.messageThumb = messageThumb;
        this.timeStamp = timeStamp;
        this.read = read;
        this.type = type;
        this.from = from;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessageThumb() {
        return messageThumb;
    }

    public void setMessageThumb(String messageThumb) {
        this.messageThumb = messageThumb;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getRead() {
        return read;
    }

    public void setRead(String read) {
        this.read = read;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }
}
