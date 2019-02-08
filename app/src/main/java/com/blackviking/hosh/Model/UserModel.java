package com.blackviking.hosh.Model;

/**
 * Created by Scarecrow on 2/2/2019.
 */

public class UserModel {

    private String userName;
    private String email;
    private String phone;
    private String sex;
    private String dateOfBirth;
    private String occupation;
    private String education;
    private String onlineState;
    private String locationVisible;
    private String dateJoined;
    private String twitter;
    private String instagram;
    private String facebook;
    private String bio;
    private String profilePicture;
    private String profilePictureThumb;
    private String userType;

    public UserModel() {
    }

    public UserModel(String userName, String email, String phone, String sex, String dateOfBirth, String occupation, String education, String onlineState, String locationVisible, String dateJoined, String twitter, String instagram, String facebook, String bio, String profilePicture, String profilePictureThumb, String userType) {
        this.userName = userName;
        this.email = email;
        this.phone = phone;
        this.sex = sex;
        this.dateOfBirth = dateOfBirth;
        this.occupation = occupation;
        this.education = education;
        this.onlineState = onlineState;
        this.locationVisible = locationVisible;
        this.dateJoined = dateJoined;
        this.twitter = twitter;
        this.instagram = instagram;
        this.facebook = facebook;
        this.bio = bio;
        this.profilePicture = profilePicture;
        this.profilePictureThumb = profilePictureThumb;
        this.userType = userType;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getOccupation() {
        return occupation;
    }

    public void setOccupation(String occupation) {
        this.occupation = occupation;
    }

    public String getEducation() {
        return education;
    }

    public void setEducation(String education) {
        this.education = education;
    }

    public String getOnlineState() {
        return onlineState;
    }

    public void setOnlineState(String onlineState) {
        this.onlineState = onlineState;
    }

    public String getLocationVisible() {
        return locationVisible;
    }

    public void setLocationVisible(String locationVisible) {
        this.locationVisible = locationVisible;
    }

    public String getDateJoined() {
        return dateJoined;
    }

    public void setDateJoined(String dateJoined) {
        this.dateJoined = dateJoined;
    }

    public String getTwitter() {
        return twitter;
    }

    public void setTwitter(String twitter) {
        this.twitter = twitter;
    }

    public String getInstagram() {
        return instagram;
    }

    public void setInstagram(String instagram) {
        this.instagram = instagram;
    }

    public String getFacebook() {
        return facebook;
    }

    public void setFacebook(String facebook) {
        this.facebook = facebook;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public String getProfilePictureThumb() {
        return profilePictureThumb;
    }

    public void setProfilePictureThumb(String profilePictureThumb) {
        this.profilePictureThumb = profilePictureThumb;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }
}
