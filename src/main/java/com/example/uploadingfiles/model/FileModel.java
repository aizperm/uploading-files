package com.example.uploadingfiles.model;

public class FileModel {
    private String filename;

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public FileModel filename(String name) {
        filename = name;
        return this;
    }
}
