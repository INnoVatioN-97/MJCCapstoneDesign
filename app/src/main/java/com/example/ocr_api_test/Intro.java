package com.example.ocr_api_test;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
//앱 처음 실행시 나오는 스플래시 화면
public class Intro extends AppCompatActivity {
    private ImageView imgCamera;
    private Animation anim;
    Handler handler = new Handler();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        initView();
        handler.postDelayed(new Runnable() {
            public void run() {
                Intent intent = new Intent(Intro.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        }, 1500);
    }
    private void initView() {
        imgCamera = (ImageView) findViewById(R.id.introImage);
        anim = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        imgCamera.setAnimation(anim);
    }
}