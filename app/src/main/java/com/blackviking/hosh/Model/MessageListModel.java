package com.blackviking.hosh.Model;

public class MessageListModel {

    private String id;
    private String message;
    private String messageThumb;
    private String timeStamp;
    private String read;
    private String type;
    private String from;
    private String to;
    private String me;
    private String you;

    public MessageListModel() {
    }

    public MessageListModel(String id, String message, String messageThumb, String timeStamp, String read, String type, String from, String to, String me, String you) {
        this.id = id;
        this.message = message;
        this.messageThumb = messageThumb;
        this.timeStamp = timeStamp;
        this.read = read;
        this.type = type;
        this.from = from;
        this.to = to;
        this.me = me;
        this.you = you;
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

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getMe() {
        return me;
    }

    public void setMe(String me) {
        this.me = me;
    }

    public String getYou() {
        return you;
    }

    public void setYou(String you) {
        this.you = you;
    }
}
