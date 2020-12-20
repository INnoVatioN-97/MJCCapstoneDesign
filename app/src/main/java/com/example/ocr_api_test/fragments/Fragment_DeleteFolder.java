package com.example.ocr_api_test.fragments;



import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;

import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;


import com.example.ocr_api_test.R;
import com.example.ocr_api_test.StringManager;
import com.example.ocr_api_test.db.DBHelper;
import com.example.ocr_api_test.folder.FolderImageAdapter;
import com.example.ocr_api_test.folder.FolderList;

//체크박스가 있는 폴더들을 보여주는 Fragment. 폴더를 삭제할 때 보여진다
public class Fragment_DeleteFolder extends Fragment {
    DBHelper helper;
    SQLiteDatabase db;
    FolderImageAdapter adapter= new FolderImageAdapter("CHECKABLE");

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StringManager path = new StringManager();
        helper = new DBHelper(getContext());
        db = helper.getWritableDatabase();
        for(int i = 0; i < 20; i++){
            path.setDeleteFolderName(null,i);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        ViewGroup deleteView = (ViewGroup) inflater.inflate(R.layout.fragment_normal, container, false);
        GridView gridView = deleteView.findViewById(R.id.GridView01);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                gridView.setItemChecked(position,true);
            }
        } );

        Cursor cursor = db.rawQuery("select * from subject;",null);
        cursor.moveToFirst();
        int mount = cursor.getCount();
        for(int i = 0; i < mount; i++){
            adapter.addItem(new FolderList(cursor.getString(1),R.drawable.folder2_bottom));
            cursor.moveToNext();
        }
        gridView.setAdapter(adapter);
        return deleteView;
    }
}
