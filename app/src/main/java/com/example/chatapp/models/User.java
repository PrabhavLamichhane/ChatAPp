package com.example.chatapp.models;

public class User {

    String name,image,userid,status;

    public User() {
    }

    public User(String name, String image, String userid, String status) {
        this.name = name;
        this.image = image;
        this.userid = userid;
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
