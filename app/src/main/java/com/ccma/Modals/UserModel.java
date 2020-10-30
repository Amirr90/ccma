package com.ccma.Modals;

public class UserModel {
    String key;
    String image;

    public UserModel(String key, String image) {
        this.key = key;
        this.image = image;
    }

    public String getImage() {
        return image;
    }

    public String getKey() {
        return key;
    }

    public UserModel() {
    }

}
