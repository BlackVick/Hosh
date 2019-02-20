package com.blackviking.hosh.Model;

/**
 * Created by Scarecrow on 2/20/2019.
 */

public class EmojiModel {

    private String name;
    private String link;

    public EmojiModel() {
    }

    public EmojiModel(String name, String link) {
        this.name = name;
        this.link = link;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }
}
