package com.example.academyapp.Model;

public class AcademyInfo {
    private String academy_name, academy_address, academy_tel, academy_image, director_photo_url, token;
    private double x, y;

    public AcademyInfo() {
    }

    public String getAcademy_name() {
        return academy_name;
    }

    public void setAcademy_name(String academy_name) {
        this.academy_name = academy_name;
    }

    public String getAcademy_address() {
        return academy_address;
    }

    public void setAcademy_address(String academy_address) {
        this.academy_address = academy_address;
    }

    public String getAcademy_tel() {
        return academy_tel;
    }

    public void setAcademy_tel(String academy_tel) {
        this.academy_tel = academy_tel;
    }

    public String getAcademy_image() {
        return academy_image;
    }

    public void setAcademy_image(String academy_image) {
        this.academy_image = academy_image;
    }

    public String getDirector_photo_url() {
        return director_photo_url;
    }

    public void setDirector_photo_url(String director_photo_url) {
        this.director_photo_url = director_photo_url;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
