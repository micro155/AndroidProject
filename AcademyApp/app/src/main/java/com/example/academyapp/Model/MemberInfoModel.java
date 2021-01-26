package com.example.academyapp.Model;

public class MemberInfoModel {

    private String Name, phoneNumber, nickName, profile, email, type, uploadfile, academy_name, academy_tel, academy_address;

    public MemberInfoModel() {
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUploadfile() {
        return uploadfile;
    }

    public void setUploadfile(String uploadfile) {
        this.uploadfile = uploadfile;
    }

    public String getAcademy_name() {
        return academy_name;
    }

    public void setAcademy_name(String academy_name) {
        this.academy_name = academy_name;
    }

    public String getAcademy_tel() {
        return academy_tel;
    }

    public void setAcademy_tel(String academy_tel) {
        this.academy_tel = academy_tel;
    }

    public String getAcademy_address() {
        return academy_address;
    }

    public void setAcademy_address(String academy_address) {
        this.academy_address = academy_address;
    }
}
