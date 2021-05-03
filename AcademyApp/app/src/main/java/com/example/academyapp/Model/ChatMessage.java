package com.example.academyapp.Model;

public class ChatMessage {
    String text, name, photoURL;

    public ChatMessage() {

    }

    public ChatMessage(String text, String name, String photoURL) {
        this.text = text;
        this.name = name;
        this.photoURL = photoURL;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhotoURL() {
        return photoURL;
    }

    public void setPhotoURL(String photoURL) {
        this.photoURL = photoURL;
    }
}
