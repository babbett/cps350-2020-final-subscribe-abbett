package com.example.subscriptionmanager;

import android.graphics.Bitmap;

import java.io.Serializable;

public class Subscription implements Serializable{
    private String title;
    private String imageUrl;
    private Bitmap image;

    public Subscription(String title, String imageUrl) {
        this.title = title;
        this.imageUrl = imageUrl;
        this.image = null;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    public Bitmap getImage() {
        return image;
    }

}
