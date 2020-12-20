package com.example.ocr_api_test;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.example.ocr_api_test.camera.CameraActivity;
import com.example.ocr_api_test.db.DBHelper;
import com.example.ocr_api_test.folder.FolderImageAdapter;
import com.example.ocr_api_test.folder.FolderList;
//카메라 액티비티에서 왼쪽아래의 폴더이미지를 꾹 눌렀을 때 나오는 저장경로변경용 Dialog
public class DialogChoosefolder extends Dialog {

    GridView folderView;
    public DialogChoosefolder(Context context) {
        super(context);
    }
    //StringManager의 객체 path를 통해 값전달
    StringManager path = new StringManager();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialog_choosefolder);
        //조금 작은 폴더이미지를 보여주는 어댑터 생성
        FolderImageAdapter adapter = new FolderImageAdapter("SMALL");

        DBHelper helper;
        SQLiteDatabase db;
        helper = new DBHelper(getContext());
        db = helper.getWritableDatabase();
        Cursor cursor = db.rawQuery("select * from subject;",null);
        cursor.moveToFirst();
        int mount = cursor.getCount();
        for(int i = 0; i < mount; i++){
            adapter.addItem(new FolderList(cursor.getString(1),R.drawable.folder2_bottom));
            cursor.moveToNext();
        }

        folderView = findViewById(R.id.folderView);
        folderView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                cursor.moveToPosition(position);
                path.setSavePath(cursor.getString(1));
                Toast.makeText(getContext(),cursor.getString(1) + "으로 경로가 설정되었습니다",Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getContext(), CameraActivity.class);
                dismiss();
                getContext().startActivity(intent);

            }
        });
        folderView.setAdapter(adapter);
    }
}