package com.example.ocr_api_test.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    private static final String dbName = "subject.db";
    private static final int DATABASE_VERSION = 3;

    public DBHelper(Context context){
        super(context, dbName, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS subject(_id INTEGER PRIMARY KEY AUTOINCREMENT, name varchar(20) NOT NULL, unique(name))");
        db.execSQL("CREATE TABLE IF NOT EXISTS keyword(_keywordId INTEGER PRIMARY KEY AUTOINCREMENT, keywordName varchar(10), subjectName varchar(20)," +
                "CONSTRAINT subjectName_fk FOREIGN KEY(subjectName) REFERENCES subject(name))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS subject");
        db.execSQL("DROP TABLE IF EXISTS keyword");
        onCreate(db);
    }

}
