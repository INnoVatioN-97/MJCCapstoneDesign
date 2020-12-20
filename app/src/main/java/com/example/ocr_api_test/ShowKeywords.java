package com.example.ocr_api_test;

import android.app.Dialog;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ocr_api_test.db.DBHelper;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;
//폴더의 키워드를 관리하는 Dialog
public class ShowKeywords extends Dialog {
    public static final int DONE = 6;
    Button btnAdd, btnSave;
    ListView keywordList;
    List<String> list;
    List<String> list_keyword;
    ArrayAdapter<String> keywordAdapter;
    StringManager path;
    String subjectName;
    TextView title; //다이얼로그 상단 텍스트바
    String subject;

    //SQLITE관련
    DBHelper helper;
    SQLiteDatabase db;
    Cursor cursor;

    public ShowKeywords(@NonNull Context context) {
        super(context);
    }

    public ShowKeywords( Context context, String subjectName){
        super(context);
        subject = subjectName;
    }
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialog_keword);
        path = new StringManager();

        btnAdd = findViewById(R.id.btnAdd);
        btnSave = findViewById(R.id.btnSave);
        keywordList = findViewById(R.id.keywordList);
        title = findViewById(R.id.dialogTitle);

        helper = new DBHelper(getContext());
        db = helper.getWritableDatabase();

        list_keyword = new ArrayList<>();
        subjectName = path.getChoosedFolderName();
        if(subjectName == null) subjectName = subject;
        title.setText("[" + subjectName + "]의 키워드 목록");
        cursor = db.rawQuery("select keywordName from keyword where subjectName='"+subjectName+"';", null);
        cursor.moveToFirst();
        for (int i = 0; i < cursor.getCount(); i++) {
            list_keyword.add(i, cursor.getString(0));
            cursor.moveToNext();
            Log.d("KEYWORD", "list_keyword" + i + " = " + list_keyword.get(i));
        }

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText keyword = new EditText(getContext());
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setView(keyword);
                builder.setPositiveButton("돌아가기", null)
                        .setNegativeButton("키워드 추가", (dialog, which) -> addKeyword(subjectName, keyword));
                builder.create().show();
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        keywordAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_list_item_1, list_keyword);
        keywordAdapter.notifyDataSetChanged();
        keywordList.setAdapter(keywordAdapter);
        Log.d("KEYWORD", "list_keyword.add succeed");

        keywordList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //키워드 수정시 사용할 EditText
                EditText keyword = new EditText(getContext());
                keyword.setText((String)parent.getItemAtPosition(position));
                AlertDialog.Builder listBuilder = new AlertDialog.Builder(getContext());
                listBuilder.setView(keyword);
                listBuilder.setPositiveButton("키워드 삭제", (dialog, which) -> deleteKeyword(position))
                        .setNegativeButton("키워드 수정", (dialog, which) ->
                        {
                            String subjectName = (String)parent.getItemAtPosition(position);
                            deleteKeyword(position);
                            addKeyword(subjectName,keyword);
                            Toast.makeText(getContext(), "수정한 키워드는 최하단에 표시됩니다.", Toast.LENGTH_LONG).show();
                        });
                listBuilder.create().show();
            }
        });
    }

    private void addKeyword(String subjectName, EditText keyword){
        String k = keyword.getText().toString();
        if(k==null){
            Toast.makeText(getContext(), "최소한1글자 이상 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }
        db.execSQL("INSERT INTO keyword(keywordName, subjectName) values('"+k+"', '"+ subjectName +"')");
        list_keyword.add(k);
        keywordAdapter.notifyDataSetChanged();
    }

    private void deleteKeyword(int position){
        String keywordName = list_keyword.get(position); //눌린 키워드명 가져옴.
        db.execSQL("delete from keyword where keywordName='"+keywordName+"'");
        list_keyword.remove(position);
        keywordAdapter.notifyDataSetChanged();
    }

}
