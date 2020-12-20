package com.example.ocr_api_test;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;

import com.bumptech.glide.Glide;


//네비게이션 뷰의 개발자들을 누르면 나오는 gif파일
public class ShowInventors extends AppCompatActivity {

    private Button btnSkipInventors;
    private ImageView ivInventors;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventors);
        getWindow().setGravity(Gravity.CENTER);
        btnSkipInventors = findViewById(R.id.btnSkipInventors);
        ivInventors = findViewById(R.id.ivInventors);
        Intent intent = new Intent(this, MainActivity.class);
        btnSkipInventors.setOnClickListener(v -> {
            startActivity(intent);
        });
//        Glide.with(this).asGif().load(R.drawable.inventors).into(ivInventors);
//        Glide.with(this).load(R.drawable.inventorshd).into(ivInventors);
        Glide.with(this).load(R.drawable.hdinventors).into(ivInventors);
        Handler hand = new Handler();
        hand.postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(intent);
                finish();
            }
        }, 20000);



    }
}
