package com.example.uploadingfiles.model;

public class FileModel {
    private String filename;
    private String url;

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

    public String getUrl() {
        return url;
    }

    public FileModel setUrl(String url) {
        this.url = url;
        return this;
    }
}
