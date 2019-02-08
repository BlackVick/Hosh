package com.blackviking.hosh.Model;

/**
 * Created by Scarecrow on 2/8/2019.
 */

public class CommentModel {

    private String comment;
    private String commenter;
    private String commentTime;

    public CommentModel() {
    }

    public CommentModel(String comment, String commenter, String commentTime) {
        this.comment = comment;
        this.commenter = commenter;
        this.commentTime = commentTime;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getCommenter() {
        return commenter;
    }

    public void setCommenter(String commenter) {
        this.commenter = commenter;
    }

    public String getCommentTime() {
        return commentTime;
    }

    public void setCommentTime(String commentTime) {
        this.commentTime = commentTime;
    }
}
