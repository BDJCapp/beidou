package com.beyond.beidou.entites;

public class FileItem {
    private String fileName;

    public FileItem(String fileName) {
        this.fileName = fileName;
    }

    public FileItem() {
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public String toString() {
        return "file{" +
                "fileName='" + fileName + '\'' +
                '}';
    }
}
