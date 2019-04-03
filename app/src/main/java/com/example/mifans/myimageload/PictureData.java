package com.example.mifans.myimageload;

public class PictureData {
    private String who;
    private String url;

    public PictureData(String who, String url) {
        this.who = who;
        this.url = url;
    }

    public String getWho() {
        return who;
    }

    public String getUrl() {
        return url;
    }
}
