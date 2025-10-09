package com.padmasiniAdmin.padmasiniAdmin_1.model;

public class Image {
    private String url;
    private String name;

    public Image() {}

    public Image(String url, String name) {
        this.url = url;
        this.name = name;
    }

    public String getUrl() {
        return url;
    }
    public void setUrl(String url) {
        this.url = url;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Image{url='" + url + "', name='" + name + "'}";
    }
}
