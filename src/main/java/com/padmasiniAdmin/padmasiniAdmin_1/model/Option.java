package com.padmasiniAdmin.padmasiniAdmin_1.model;

public class Option {
    private String text;
    private String image; // S3 URL

    public Option() {}

    public Option(String text, String image) {
        this.text = text;
        this.image = image;
    }

    public String getText() {
        return text;
    }
    public void setText(String text) {
        this.text = text;
    }

    public String getImage() {
        return image;
    }
    public void setImage(String image) {
        this.image = image;
    }

    @Override
    public String toString() {
        return "Option [text=" + text + ", image=" + image + "]";
    }
}
