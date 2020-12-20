package com.example.ocr_api_test;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.example.ocr_api_test.camera.CameraActivity;
import com.example.ocr_api_test.db.DBHelper;
import com.example.ocr_api_test.fragments.Fragment_DeleteFolder;
import com.example.ocr_api_test.fragments.Fragment_Standard;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final int GALLERY_PERMISSIONS_REQUEST = 0;
    private static final int GALLERY_IMAGE_REQUEST = 1;
    public static final int CAMERA_PERMISSIONS_REQUEST = 2;
    public static final int CAMERA_IMAGE_REQUEST = 3;
    public static final int PERMISSIONS_REQUEST_CODE = 4;
    //추가 (10월28일 3시 45분)
    public static final int SHOWSUBJECTS = 5;
    public static final int DONE = 6;

    private TextView mImageDetails; //상단에 띄울 TextView
    private ImageView mMainImage; //불러온 사진을 띄울 ImageView

    public static Context m_context;
    private MediaScanner ms;
    //핸들러 메시지
    public static final int START_PROGRESSBAR = 7;
    public static final int END_PROGRESSBAR = 8;
    public static final int REFRESH_RECENTFOLDERNAME = 9;

    DBHelper helper;
    SQLiteDatabase db;
    Cursor cursor;
    //폴더삭제버튼 관련
    boolean goToChooseMode = true;
    //StringManager의 객체 path를 통해 값 전달
    StringManager path = new StringManager();
    //상단 메뉴
    public static boolean OCR_ON = false;
    private ImageView addFolder;
    private ImageView deleteFolder;
    private CompoundButton OCR_Switch;
    private NavigationView navigationView;
    //하단 메뉴
    private ImageView ivCamera, ivUseOcr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestPermission();

        ms = MediaScanner.newInstance(MainActivity.this);

        helper = new DBHelper(this);
        db = helper.getWritableDatabase();

        m_context = this;

        SharedPreferences pref = getSharedPreferences("checkFirst", Activity.MODE_PRIVATE);
        //앱을 처음 사용하는 사람에게는 TutrialActivity를 보여준다
        boolean checkFirst = pref.getBoolean("checkFirst", false);
        if (checkFirst == false) {
            SharedPreferences.Editor editor = pref.edit();
            editor.putBoolean("checkFirst", true);
            editor.commit();

            Intent intent = new Intent(MainActivity.this, TutorialActivity.class);
            startActivity(intent);
            finish();
        } else {
            setStandardFragment();
            setSubjects();

            ivCamera = findViewById(R.id.ivCamera); //카메라 버튼
            ivUseOcr = findViewById(R.id.ivUseOcr); //기기의 내부저장소로 이동
            ivCamera.setOnClickListener(v -> startCamera());
            ivUseOcr.setOnClickListener(v -> startGalleryChooser());

            addFolder = findViewById(R.id.btnAddFolder);
            deleteFolder = findViewById(R.id.btnDeleteFolder);
            OCR_Switch = findViewById(R.id.toggleOCR);
            ImageView.OnClickListener onClickListener = new ImageView.OnClickListener() {
                @Override
                public void onClick(View view) {
                    switch (view.getId()) {
                        case R.id.btnAddFolder: //폴더생성버튼
                            final Dialog addFolderDialog = new Dialog(m_context);
                            addFolderDialog.setContentView(R.layout.dialog_addfolder);
                            Button ok = addFolderDialog.findViewById(R.id.createFolder);
                            Button cancel = addFolderDialog.findViewById(R.id.cancel_createFolder);

                            final EditText folderName = addFolderDialog.findViewById(R.id.folderName);
                            // 예 버튼 누르면
                            ok.setOnClickListener(v -> {
                                //폴더명 공백 or 중복 검사 통과하면 폴더생성
                                cursor = db.rawQuery("select * from subject;", null);
                                cursor.moveToFirst();
                                String folder_name = folderName.getText().toString();
                                Boolean OK = true;
                                if (folder_name.trim().length() == 0) { //폴더명이 공백인지 검사
                                    Toast.makeText(getApplicationContext(), "폴더이름을 입력해주세요", Toast.LENGTH_SHORT).show();
                                    OK = false;
                                    mImageDetails.setText(null);
                                    mMainImage.setImageBitmap(null);
                                }
                                for (int i = 0; i < cursor.getCount(); i++) { //폴더명이 중복되는지 검사
                                    if (folder_name.equals(cursor.getString(1))) {
                                        Toast.makeText(getApplicationContext(), "동일한 폴더명이 존재합니다", Toast.LENGTH_SHORT).show();
                                        OK = false;
                                        break;
                                    }
                                    cursor.moveToNext();
                                }
                                if (OK) { //폴더명 검사에 통과하면 폴더 생성
                                    db.execSQL("insert into subject(name) values('" + folder_name + "');");
                                    Toast.makeText(getBaseContext(), folderName.getText() + "폴더 생성 성공", Toast.LENGTH_LONG).show();
                                    setSubjects(); //DB에 추가된 과목정보로 Subject 배열을 갱신.
                                    addFolderDialog.dismiss();
                                    getSaveFolder(folder_name);
                                    setStandardFragment();
                                }
                            });
                            // 아니오 버튼 누르면
                            cancel.setOnClickListener(v -> {
                                Toast.makeText(getBaseContext(), "폴더 생성 취소", Toast.LENGTH_LONG).show();
                                addFolderDialog.dismiss();
                            });
                            addFolderDialog.show(); //대화상자 띄우기
                            break;

                        case R.id.btnDeleteFolder: //폴더삭제버튼(휴지통)
                            if (goToChooseMode) {
                                setDeleteFragment();
                                Toast.makeText(getBaseContext(), "삭제할 폴더고르는 화면", Toast.LENGTH_LONG).show();
                                goToChooseMode = false;
                            } else {
                                cursor = db.rawQuery("select * from subject", null);
                                String tmp; //폴더 삭제시 그 폴더의 이름을 담을 변수.
                                for (int i = 0; i < cursor.getCount(); i++) {
                                    tmp = path.getDeleteFolderName(i);
                                    if (tmp == null) {
                                    } else {
                                        //SQLITE에서 해당되는 폴더와 폴더의 키워드들 삭제
                                        db.execSQL("delete from subject where name = '" + tmp + "';");
                                        db.execSQL("delete from keyword where subjectname = '" + tmp + "';");
                                        removeDir(tmp); //실제 폴더와 그 안의 사진들 삭제
                                        Toast.makeText(m_context, "삭제되었습니다", Toast.LENGTH_SHORT).show();
                                    }
                                }
                                setStandardFragment();
                                setSubjects(); // SQLite의 Subject 테이블 새로고침
                                goToChooseMode = true;
                                break;
                            }
                    }
                }
            };
            addFolder.setOnClickListener(onClickListener);
            deleteFolder.setOnClickListener(onClickListener);

            OCR_Switch.setOnCheckedChangeListener((compoundButton, flag) -> {
                if (flag) {
                    Toast.makeText(m_context, "OCR 기능을 이용해 찍은 사진에 맞는\n과목 폴더를 지정합니다.", Toast.LENGTH_SHORT).show();
                    OCR_ON = true;
                } else {
                    Toast.makeText(m_context, "OCR 사용을 해제합니다", Toast.LENGTH_SHORT).show();
                    OCR_ON = false;
                }
            });
        }
        //네비게이션 뷰 등록
        navigationView = findViewById(R.id.navigation);
        navigationView.setNavigationItemSelectedListener(this::onNavigationItemSelected);
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Intent intent;
        if (item.getItemId() == R.id.viewTutorial) {
            intent = new Intent(MainActivity.this, TutorialActivity.class);
            startActivity(intent);
            finish();
        } else if (item.getItemId() == R.id.viewInventors) {
            intent = new Intent(MainActivity.this, ShowInventors.class);
            startActivity(intent);
            finish();
        }
        DrawerLayout drawer = findViewById(R.id.drawer);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    //메인화면에 표시될 fragment를 정하는 함수들
    public void setStandardFragment() {
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction ft = manager.beginTransaction();
        ft.replace(R.id.gridView_container, new Fragment_Standard());
        ft.commitNowAllowingStateLoss();
    }
    public void setDeleteFragment() {
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction ft = manager.beginTransaction();
        ft.replace(R.id.gridView_container, new Fragment_DeleteFolder());
        ft.commitNowAllowingStateLoss();
    }
    //외부 저장소 (쓰기)를 사용하기 위한 권한 요청을 위한 메소드
    private void requestPermission() {
        boolean shouldProviceRational = ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (shouldProviceRational) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_CODE);
            Log.d("TAG", "Permission requesting");
        } else {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_CODE);
            Log.d("TAG", "Permission get at line 87");
        }
    }
    //저장소 권한 허가 Dialog
    public void denialDialog() {
        new AlertDialog.Builder(this)
                .setTitle("알림")
                .setMessage("저장소 권한이 필요합니다. 환경 설정에서 저장소 권한을 허가해주세요.")
                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent savePermissionIntent = new Intent();
                        savePermissionIntent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package",
                                BuildConfig.APPLICATION_ID, null);
                        savePermissionIntent.setData(uri);
                        savePermissionIntent.setFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(savePermissionIntent); //확인버튼누르면 바로 어플리케이션 권한 설정 창으로 이동하도록
                    }
                })
                .create()
                .show();
    }

    //내부저장소로 화면전환
    public void startGalleryChooser() {
        if (PermissionUtils.requestPermission(this, GALLERY_PERMISSIONS_REQUEST, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select a photo"),
                    GALLERY_IMAGE_REQUEST);
        }
    }
    //사진 찍는 화면으로 전환
    public void startCamera() {
        if (PermissionUtils.requestPermission(this,
                CAMERA_PERMISSIONS_REQUEST,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA)) {

            Intent startCameraIntent;
            startCameraIntent = new Intent(this, CameraActivity.class);
            startActivityForResult(startCameraIntent, CAMERA_IMAGE_REQUEST);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //내부저장소에 있는 사진 터치시
        if (requestCode == GALLERY_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
//            uploadImage(data.getData());
//            path.setPhotoUri(data.getData());
//            Bitmap bitmap = null;
//            try {
//                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),path.getPhotoUri());
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            byte[] bytes;
//            ByteArrayOutputStream stream = new ByteArrayOutputStream();
//            bytes = stream.toByteArray();
//            bitmap.compress(Bitmap.CompressFormat.JPEG,50,stream);
//            OCR_UseUri ocr = new OCR_UseUri(bytes);
//            String ocrFolderName = ocr.doOcrInGallery();
//            Toast.makeText(this,ocrFolderName + "으로 검출되었습니다",Toast.LENGTH_SHORT).show();
//            //내부저장소에 있는 사진에 OCR을 돌리는 메소드로 OCR을 메인스레드로 돌리기 때문에
//            //uri형식의 파일을 주고받을수 없다
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CAMERA_PERMISSIONS_REQUEST:
                if (PermissionUtils.permissionGranted(requestCode, CAMERA_PERMISSIONS_REQUEST, grantResults)) {
                    startCamera();
                }
                break;
            case GALLERY_PERMISSIONS_REQUEST:
                if (PermissionUtils.permissionGranted(requestCode, GALLERY_PERMISSIONS_REQUEST, grantResults)) {
                    startGalleryChooser();
                }
                break;
            case PERMISSIONS_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("TAG", "Permission get at lint 250");
                } else {
                    //사용자가 권한 거절시
                    denialDialog();
                }
                return;
            }
        }
    }
    /* OCR을 백그라운드에서 스레드로 돌리면 사용 가능한 메소드 */
//    public void uploadImage(Uri uri) {
//        progress.setTitle("로딩중");
//        progress.setMessage("사진을 분석 중입니다...");
//        progress.show();
//        if (uri != null) {
//            try {
//                // scale the image to save on bandwidth
//                Bitmap bitmap =
//                        scaleBitmapDown(
//                                MediaStore.Images.Media.getBitmap(getContentResolver(), uri),
//                                MAX_DIMENSION);
//                callCloudVision(bitmap);
//                mMainImage.setImageBitmap(bitmap);
//            } catch (IOException e) {
//                Log.d(TAG, "Image picking failed because " + e.getMessage());
//                progress.cancel();
//                Toast.makeText(this, R.string.image_picker_error, Toast.LENGTH_LONG).show();
//            }
//        } else {
//            Log.d(TAG, "Image picker gave us a null image.");
//            progress.cancel();
//            Toast.makeText(this, R.string.image_picker_error, Toast.LENGTH_LONG).show();
//        }
//    }

    //사진을 저장할 디렉토리를 생성해주는 메소드
    public File getSaveFolder(String subject) {
        String root = Environment.getExternalStorageDirectory().getAbsolutePath();
        File dir = new File(root + "/" + subject);
        if (!dir.exists()) {
            boolean wasSuccessful = dir.mkdir();
            if (!wasSuccessful) Log.d("TAG", "dir.mkdir Failed");
            else Log.d("TAG", "dir.mkdir() Succeed");
        } else System.out.println("file: " + root + "/" + subject + "already exists");
        return dir;
    }
    //지정한 폴더를 삭제해주는 메소드
    public void removeDir(String dirName) {
        String deletePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + dirName;
        File folder = new File(deletePath);
        try {
            if (folder.exists()) {
                File[] folder_list = folder.listFiles(); //파일리스트 얻어오기
                for (int i = 0; i < folder_list.length; i++) {
                    if (folder_list[i].isFile()) {
                        folder_list[i].delete();
                        System.out.println("파일이 삭제되었습니다.");
                    } else {
                        removeDir(folder_list[i].getPath()); //재귀함수호출
                        System.out.println("폴더가 삭제되었습니다.");
                    }
                    folder_list[i].delete();
                }
                ms.mediaScanning(deletePath);
                folder.delete(); //폴더 삭제
            }
        } catch (Exception e) {
            e.getStackTrace();
        }
    }
    /* SQLite의 Subject 테이블을 새로고침하고 현재 존재하는 과목을
      StringManager의 SUBJECTS 배열에 다시 넣어주는 메소드      */
    public void setSubjects() {
        cursor = db.rawQuery("Select * from subject;", null);
        cursor.moveToFirst();
        int countOfSubjects = cursor.getCount(); //과목 테이블 카디널리티 (튜플 수) = 과목갯수
        path.SUBJECTS = new String[countOfSubjects];
        for (int i = 0; i < countOfSubjects; i++) {
            path.SUBJECTS[i] = cursor.getString(1);
            System.out.println("SUBJECTS[" + i + "]= " + path.SUBJECTS[i]);
            cursor.moveToNext();
        }
    }
    //왼쪽 상단의 Information 버튼
    public void information(View view) {
        DrawerLayout drawer = findViewById(R.id.drawer);
        drawer.openDrawer(Gravity.START);
    }

    public boolean getOCRState() {
        return this.OCR_ON;
    }
    public void setOCRState(boolean state){
        this.OCR_ON = state;
    }
}

