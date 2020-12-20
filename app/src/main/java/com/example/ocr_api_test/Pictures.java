package com.example.ocr_api_test;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.Picture;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
//폴더 속 사진들을 보여주는 클래스
public class Pictures extends AppCompatActivity {
    private List<File> mediaFiles = new ArrayList<>();
    private List<File> mediaFiles2 = new ArrayList<>();
    private MediaFileAdapter m_adapter;
    private DeletePictureAdapter m_adapter2;
    private static boolean isdeletemode;
    private String savePath;
    private GridView gallery;
    private File[] deleteFiles;
    private int pictureCount;
    private static boolean finishdeletemode;
    private MediaScanner mediaScanner;
    private TextView tvSubjectName;
    private String folderName;
    ImageView deletePictures;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pictures);
        gallery = findViewById(R.id.gallery);
        tvSubjectName = findViewById(R.id.tvSubjectName);
        finishdeletemode = false;
        isdeletemode = false;
        pictureCount = 0;
        deleteFiles = new File[30];
        mediaScanner = MediaScanner.newInstance(Pictures.this);
        deletePictures = findViewById(R.id.deletePictures);

        Intent intent = getIntent();
        folderName = intent.getStringExtra("folderName");
        tvSubjectName.setText(folderName);
        savePath = Environment.getExternalStorageDirectory()+"/"+folderName;

        m_adapter = new MediaFileAdapter(this, mediaFiles);
        m_adapter2 = new DeletePictureAdapter(this, mediaFiles2);

        gallery.setAdapter(m_adapter);
        onResume();

        gallery.setOnItemClickListener((parent, view, position, id) -> {
            File file = m_adapter.getItem(position);
            playOrViewMedia(file);
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!isdeletemode) {
            File file = new File(savePath);
            if (file.isDirectory()) {
                mediaFiles.clear();
                File[] files = file.listFiles();
                Arrays.sort(files, (f1, f2) -> {
                    if (f1.lastModified() - f2.lastModified() == 0) {
                        return 0;
                    } else {
                        return f1.lastModified() - f2.lastModified() > 0 ? -1 : 1;
                    }
                });
                mediaFiles.addAll(Arrays.asList(files));
                m_adapter.notifyDataSetChanged();
            }
        }
        else{
            File file2 = new File(savePath);
            if (file2.isDirectory()) {
                mediaFiles2.clear();
                File[] files2 = file2.listFiles();
                Arrays.sort(files2, (f1, f2) -> {
                    if (f1.lastModified() - f2.lastModified() == 0) {
                        return 0;
                    } else {
                        return f1.lastModified() - f2.lastModified() > 0 ? -1 : 1;
                    }
                });
                mediaFiles2.addAll(Arrays.asList(files2));
                m_adapter2.notifyDataSetChanged();
            }
        }
        isdeletemode=false;
    }
    private void playOrViewMedia(File file) {  //저장된 사진 사진보기, 띄우기
        Intent intent2 = new Intent(Intent.ACTION_VIEW);
        Uri uriforfile = FileProvider.getUriForFile(Pictures.this, getPackageName() + ".provider", file);

        intent2.setDataAndType(uriforfile, isVideo(file) ? "video/mp4" : "image/jpg");
        intent2.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        PackageManager packageManager = getPackageManager();
        List<ResolveInfo> activities = packageManager.queryIntentActivities(intent2, 0);
        boolean isIntentSafe = activities.size() > 0;

        if (isIntentSafe) {
            startActivity(intent2);
        } else {
            Toast.makeText(Pictures.this, "No media viewer found", Toast.LENGTH_SHORT).show();
        }
    }

    private class MediaFileAdapter extends BaseAdapter {
        private List<File> files;
        private Context context;

        MediaFileAdapter(Context c, List<File> files) {
            context = c;
            this.files = files;
        }

        public int getCount() {
            return files.size();
        }

        public File getItem(int position) {
            return files.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView;
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.item_media, parent, false);
            }
            imageView = convertView.findViewById(R.id.item_image);
            View indicator = convertView.findViewById(R.id.item_indicator);
            File file = getItem(position);
            if (isVideo(file)) {
                indicator.setVisibility(View.VISIBLE);
            } else {
                indicator.setVisibility(View.GONE);
            }
            Glide.with(context).load(file).into(imageView);
            return convertView;
        }
    }

    public class DeletePictureAdapter extends BaseAdapter {
        private List<File> files2;
        private Context context2;

        DeletePictureAdapter(Context c, List<File> files) {
            context2 = c;
            this.files2 = files;
        }

        public int getCount() {
            return files2.size();
        }

        public File getItem(int position) {
            return files2.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView;
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.item_media_checkbox, parent, false);
            }
            imageView = convertView.findViewById(R.id.item_image_check);
            View indicator = convertView.findViewById(R.id.item_indicator_check);
            CheckBox checkBox = convertView.findViewById(R.id.item_image_checkbox);
            File file = getItem(position);
            if (isVideo(file)) {
                indicator.setVisibility(View.VISIBLE);
            } else {
                indicator.setVisibility(View.GONE);
            }
            Glide.with(context2).load(file).into(imageView);
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked){
                    if(isChecked){
                        pictureCount++;
                        deleteFiles[position] = getItem(position);
                    }
                    else{
                        pictureCount--;
                        deleteFiles[position]=null;
                    }
                }
            });
            return convertView;
        }
    }

    private boolean isVideo(File file) {
        return file != null && file.getName().endsWith(".mp4");
    }

    //이미지 삭제할건지 묻는 다이얼로그
    public void checkDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(Pictures.this);
        builder.setTitle(R.string.dialog_check);
        builder.setPositiveButton("취소", null)
                .setNegativeButton("삭제", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //사진 삭제코드
                        for(int k=0; k<deleteFiles.length; k++) {
                            if(!(deleteFiles[k] == null))
                                deleteFiles[k].delete();
                        }
                        mediaScanner.mediaScanning(savePath);
                        pictureCount=0;
                        gallery.setAdapter(m_adapter);
                        isdeletemode = false;
                        onResume();
                    }
                });
        builder.create().show();
    }

    // 화살표 버튼 onClick
    public void goBack(View view){
        Intent intent = new Intent(Pictures.this,MainActivity.class);
        startActivity(intent);
    }

    // 휴지통 버튼 onClick
    public void delete(View view){
        if(pictureCount==0) {
            finishdeletemode = false;
            gallery.setAdapter(m_adapter);
            onResume();
        } else {
            checkDialog();
        }
    }

    public void showMenu(View view){
        PopupMenu popup = new PopupMenu(Pictures.this, view);
        popup.getMenuInflater().inflate(R.menu.picture_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch(item.getItemId()){
                    case R.id.erasePicture: //사진 삭제 메뉴 아이템
                        if(!finishdeletemode) {
                            for(int i=0; i<deleteFiles.length; i++)
                                deleteFiles[i] = null;
                            finishdeletemode = true;
                            isdeletemode = true;
                            gallery.setAdapter(m_adapter2);
                            if(m_adapter.getCount() > 0)
                                deletePictures.setVisibility(View.VISIBLE);


                            onResume();
                        }else {
                            if(pictureCount==0) {
                                finishdeletemode = false;
                                gallery.setAdapter(m_adapter);
                                onResume();
                            } else {
                                checkDialog();
                            }
                        }
                        return true;
                    case R.id.manageKeyword: //키워드관리 메뉴 아이템
                        ShowKeywords keywordDialog = new ShowKeywords(Pictures.this, folderName);
                        keywordDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        keywordDialog.show();
                        return true;
                }
                return true;
            }
        });
        popup.show();
    }
}
