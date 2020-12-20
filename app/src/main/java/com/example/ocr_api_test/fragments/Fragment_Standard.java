package com.example.ocr_api_test.fragments;


import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.PopupMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.example.ocr_api_test.Pictures;
import com.example.ocr_api_test.R;
import com.example.ocr_api_test.ShowKeywords;
import com.example.ocr_api_test.StringManager;
import com.example.ocr_api_test.db.DBHelper;
import com.example.ocr_api_test.folder.FolderImageAdapter;
import com.example.ocr_api_test.folder.FolderList;
//메인화면에 보여지는 폴더이미지 Fragment
public class Fragment_Standard extends Fragment {

    DBHelper helper;
    SQLiteDatabase db;
    //체크박스 없는 기본 폴더 그리드뷰로 생성
    FolderImageAdapter adapter = new FolderImageAdapter("NORMAL");
    //StringManager의 객체 path를 통해 값전달
    StringManager path = new StringManager();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        helper = new DBHelper(getContext());
        db = helper.getWritableDatabase();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        ViewGroup standardView = (ViewGroup) inflater.inflate(R.layout.fragment_normal, container, false);
        GridView gridView = standardView.findViewById(R.id.GridView01);

        Cursor cursor = db.rawQuery("select * from subject;",null);
        cursor.moveToFirst();
        int mount = cursor.getCount();
        for(int i = 0; i < mount; i++){
            adapter.addItem(new FolderList(cursor.getString(1),R.drawable.folder2_bottom));
            cursor.moveToNext();
        }

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                Intent intent = new Intent(getContext(), Pictures.class);
                cursor.moveToPosition(position);
                intent.putExtra("folderName",cursor.getString(1));
                startActivity(intent);
            }
        });
        //폴더를 꾹 눌렀을 때 popup 메뉴를 보여준다
        gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long l) {
                PopupMenu popup = new PopupMenu(getContext(), view);
                popup.getMenuInflater().inflate(R.menu.folder_popup, popup.getMenu());
                popup.setOnMenuItemClickListener(
                        new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                switch(item.getItemId()) {
                                    case R.id.edit: //이 폴더를 경로로 지정 버튼
                                        cursor.moveToPosition(position);
                                        path.setSavePath(cursor.getString(1));
                                        Toast.makeText(getContext(), "경로가 [" + path.getRecentFolderName() + "]으로 지정되었습니다", Toast.LENGTH_SHORT).show();
                                        break;
                                    case R.id.manageKeword: //키워드 관리 버튼
                                        cursor.moveToPosition(position);
                                        path.setChoosedFolderName(cursor.getString(1));
                                        //키워드 관리 다이얼로그 show
                                        ShowKeywords keywordDialog = new ShowKeywords(getContext());
                                        keywordDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                        keywordDialog.show();
                                        break;
                                    default:
                                        return true;
                                }
                                return true;
                            }
                        });
                popup.show();
                return true;
            }
        });
        gridView.setAdapter(adapter);
        return standardView;
    }
}





