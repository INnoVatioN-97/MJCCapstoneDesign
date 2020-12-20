package com.example.ocr_api_test;

import android.net.Uri;
import android.os.Environment;
//여러군데에서 필요한 값들은 StringManager를 통해서 get/set 할수있습니다
public class StringManager{
    public static StringManager mContext;
    public static String[] deleteFolderName = new String[20];
    public static String savePath ="";
    public static String savePathFolderName;
    public static String ROOTPATH = Environment.getExternalStorageDirectory().getPath() + "/";
    public static String choosedFolderName;
    public static String[] SUBJECTS; //각각의 과목명이 들어올 배열
    public static String[][] KEYWORD; // [과목별로][해당되는 키워드]가 저장될 2차원 문자열 배열


    public static Uri photoUri;

    public StringManager(){ mContext = this; }

    //setter
    public static void setPhotoUri(Uri photoUri) {
        StringManager.photoUri = photoUri;
    }
    public static void setSavePathFolderName(String savePathFolderName) {
        StringManager.savePathFolderName = savePathFolderName;
    }
    public void setSavePath(String folderName) {
        savePathFolderName = folderName;
        savePath = ROOTPATH + folderName;
    }
    public void setFullSavePath(String fullPathString){
        savePath = fullPathString;
    }
    public void setDeleteFolderName(String foldername,int position) {
        this.deleteFolderName[position] = foldername;
    }
    public static void setChoosedFolderName(String choosedFolderName) {
        StringManager.choosedFolderName = choosedFolderName;
    }

    //getter
    public String getDeleteFolderName(int position) {
        return deleteFolderName[position];
    }
    public String getSavePath() {
        return savePath;
    }
    public String getRecentFolderName() {
        return savePathFolderName;
    }
    public static String getChoosedFolderName() {
        return choosedFolderName;
    }
    public static Uri getPhotoUri() {
        return photoUri;
    }








}