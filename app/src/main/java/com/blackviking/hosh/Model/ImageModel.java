package com.blackviking.hosh.Model;

/**
 * Created by Scarecrow on 2/5/2019.
 */

public class ImageModel {

    private String imageUrl;
    private String imageThumbUrl;
    private String imageState;

    public ImageModel() {
    }

    public ImageModel(String imageUrl, String imageThumbUrl, String imageState) {
        this.imageUrl = imageUrl;
        this.imageThumbUrl = imageThumbUrl;
        this.imageState = imageState;
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

    public String getImageState() {
        return imageState;
    }

    public void setImageState(String imageState) {
        this.imageState = imageState;
    }
}
