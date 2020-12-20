package com.example.ocr_api_test.folder;

public class FolderList {
    String folderName;
    Integer folderImage;

    public FolderList(String folderName, Integer folderImage){
        this.folderName = folderName;
        this.folderImage = folderImage;
    }
    public String getFolderName() {
        return folderName;
    }
    public Integer getFolderImage() {
        return folderImage;
    }
}
