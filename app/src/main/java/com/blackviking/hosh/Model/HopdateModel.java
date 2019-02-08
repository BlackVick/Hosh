package com.blackviking.hosh.Model;

/**
 * Created by Scarecrow on 2/4/2019.
 */

public class HopdateModel {

    private String sourceType;
    private String title;
    private String hopdate;
    private String sender;
    private String imageUrl;
    private String imageThumbUrl;
    private String timestamp;
    private String hopdateType;

    public HopdateModel() {
    }

    public HopdateModel(String sourceType, String title, String hopdate, String sender, String imageUrl, String imageThumbUrl, String timestamp, String hopdateType) {
        this.sourceType = sourceType;
        this.title = title;
        this.hopdate = hopdate;
        this.sender = sender;
        this.imageUrl = imageUrl;
        this.imageThumbUrl = imageThumbUrl;
        this.timestamp = timestamp;
        this.hopdateType = hopdateType;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getHopdate() {
        return hopdate;
    }

    public void setHopdate(String hopdate) {
        this.hopdate = hopdate;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getImageThumbUrl() {
        return imageThumbUrl;
    }

    public void setImageThumbUrl(String imageThumbUrl) {
        this.imageThumbUrl = imageThumbUrl;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getHopdateType() {
        return hopdateType;
    }

    public void setHopdateType(String hopdateType) {
        this.hopdateType = hopdateType;
    }
}
