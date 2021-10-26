package com.beyond.beidou.entites;

public class FileSelectItem {
    private String fileName;
    private boolean isSelect = false;

    public FileSelectItem() {
    }

    public FileSelectItem(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public boolean isSelect() {
        return isSelect;
    }

    public void setSelect(boolean select) {
        isSelect = select;
    }

    @Override
    public String toString() {
        return "FileSelectItem{" +
                "fileName='" + fileName + '\'' +
                ", isSelect=" + isSelect +
                '}';
    }
}
