package com.example.academyapp.Model;

public class FileListInfo {
    String academy_name;
    String file_name;

    public FileListInfo(String academy_name, String file_name) {
        this.academy_name = academy_name;
        this.file_name = file_name;
    }

    public String getAcademy_name() {
        return academy_name;
    }

    public void setAcademy_name(String academy_name) {
        this.academy_name = academy_name;
    }

    public String getFile_name() {
        return file_name;
    }

    public void setFile_name(String file_name) {
        this.file_name = file_name;
    }
}
