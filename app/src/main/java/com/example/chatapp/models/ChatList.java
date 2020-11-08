package com.example.chatapp.models;

public class ChatList {
    public String id;

    public ChatList() {
    }

    public ChatList(String id,String myId) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


}
