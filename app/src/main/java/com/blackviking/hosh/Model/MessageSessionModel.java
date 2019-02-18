package com.blackviking.hosh.Model;

/**
 * Created by Scarecrow on 2/17/2019.
 */

public class MessageSessionModel {

    private String myId;
    private String friendId;

    public MessageSessionModel() {
    }

    public MessageSessionModel(String myId, String friendId) {
        this.myId = myId;
        this.friendId = friendId;
    }

    public String getMyId() {
        return myId;
    }

    public void setMyId(String myId) {
        this.myId = myId;
    }

    public String getFriendId() {
        return friendId;
    }

    public void setFriendId(String friendId) {
        this.friendId = friendId;
    }
}
