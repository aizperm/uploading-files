package com.example.uploadingfiles.model;

import java.util.ArrayList;
import java.util.List;

public class FilesModel {
    private List<FileModel> files;

    public List<FileModel> getFiles() {
        return files;
    }

    public void setFiles(List<FileModel> files) {
        this.files = files;
    }

    public FilesModel file(FileModel file) {
        if (files == null)
            files = new ArrayList<>();
        files.add(file);
        return this;
    }
}
