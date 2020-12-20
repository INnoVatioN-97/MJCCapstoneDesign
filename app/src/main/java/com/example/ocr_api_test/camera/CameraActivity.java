package com.example.ocr_api_test.camera;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.TextureView;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ocr_api_test.DialogChoosefolder;
import com.example.ocr_api_test.MainActivity;
import com.example.ocr_api_test.R;
import com.example.ocr_api_test.StringManager;

import static com.example.ocr_api_test.MainActivity.END_PROGRESSBAR;
import static com.example.ocr_api_test.MainActivity.START_PROGRESSBAR;

public class CameraActivity extends AppCompatActivity {
    public static TextView folderName; //현재 지정되있는 경로의 폴더명
    public static final int REQUEST_CAMERA = 1;
    private TextureView mCameraTextureView;
    private Preview mPreview;
    private Button mNormalAngleButton;
    private Button mWideAngleButton;
    ImageView captureImage;
    ImageView change;

    Activity cameraActivity = this;

    //StringManager 객체 path를 통해 값을 전달받음
    StringManager path = new StringManager();

    //핸들러
    private static ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);

        //저장경로가 지정되지 않은채로 카메라화면을 호출하면
        if(path.getSavePath()=="" && ((MainActivity) MainActivity.m_context).getOCRState() == false) {
            Toast.makeText(cameraActivity, "저장경로를 지정하세요.", Toast.LENGTH_LONG).show();
            finish();
        }
        setContentView(R.layout.activity_camera);
        progress = new ProgressDialog(this);

        //현재 지정된 폴더이름
        folderName = findViewById(R.id.edtFolderName);
        folderName.setText(path.getRecentFolderName());
        //카메라 화면
        mCameraTextureView = findViewById(R.id.cameraTextureView);
        //일반 렌즈, 광각렌즈, 촬영버튼, 방향전환버튼
        mNormalAngleButton = findViewById(R.id.normal);
        mWideAngleButton = findViewById(R.id.wide);
        captureImage = findViewById(R.id.capture);
        change = findViewById(R.id.change);

        //실질적인 카메라 화면과 촬영을 담당하는 Preview 객체로 정보 전달
        mPreview = new Preview(this, mCameraTextureView, mNormalAngleButton, mWideAngleButton,
                captureImage,change);
        mPreview.openCamera();

        //폴더버튼 onClick
        ImageView folderImage = findViewById(R.id.folderImage);
        folderImage.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CameraActivity.this, MainActivity.class);
                Toast.makeText(getBaseContext(),"폴더 선택 화면으로 이동합니다",Toast.LENGTH_SHORT).show();
                startActivity(intent);
            }
        } );
        //폴더버튼 꾹 누르면 그리드뷰(다이얼로그) 화면에 출력
        folderImage.setOnLongClickListener(new View.OnLongClickListener(){
            @Override
            public boolean onLongClick(View view) {
                DialogChoosefolder dialog = new DialogChoosefolder(CameraActivity.this);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
                Toast.makeText(getBaseContext(),"경로로 지정할 폴더를 선택해주세요",Toast.LENGTH_SHORT).show();
                return true;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPreview.onResume();

    }
    @Override
    protected void onPause(){
        super.onPause();
        mPreview.onPause();
    }

    // Preview내에서 실행되는 Thread의 Handler
    static class ValueHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle bundle = msg.getData();
            if(bundle.getInt("start") == START_PROGRESSBAR){
                progress.setTitle("로딩중");
                progress.setMessage("사진을 분석 중입니다...");
                progress.show();
            }
            else if(bundle.getInt("end") == END_PROGRESSBAR){
                progress.cancel();
                ((MainActivity) MainActivity.m_context).setOCRState(false);
            }
            else {
                String ocrFolderName = bundle.getString("ocrFolderName");
                folderName.setText(ocrFolderName);
            }
        }
    }

}



