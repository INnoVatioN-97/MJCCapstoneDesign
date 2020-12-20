package com.example.ocr_api_test.folder;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ocr_api_test.R;
import com.example.ocr_api_test.StringManager;

import java.util.ArrayList;

//메인화면에 폴더이미지를 보여주는 Adapter
public class FolderImageAdapter extends BaseAdapter {
    ArrayList<FolderList> items = new ArrayList<FolderList>();
    Context context;
    // 1 : 보통 폴더, 2 : 체크박스 폴더. 3 : 조금 작은 폴더 이미지
    public int itemCategory = 0;
    public static final int ITEM_NORMAL = 1;
    public static final int ITEM_CHECKABLE = 2;
    public static final int ITEM_SMALL = 3;
    ImageView folderImage;
    TextView folderName;
    //StringManager의 객체 path를 통해 값 전달
    StringManager path = new StringManager();
    //FolderImageAdpater가 생성될 때 Adapter가 어떤 이미지를 보여줄지 정해진다
    public FolderImageAdapter(String category) {
        if (category.equals("NORMAL")) //기본 폴더
            itemCategory = 1;
        else if (category.equals("CHECKABLE")) //체크박스가 있는 폴더
            itemCategory = 2;
        else if (category.equals("SMALL")) // 크기가 작은 폴더
            itemCategory = 3;
    }


    public void addItem(FolderList item) {
        items.add(item);
    }
    //public void delItem(FolderList item) { items.remove(item); }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        context = parent.getContext();
        FolderList listItem = items.get(position);

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            //보여줄 이미지에 맞는 layout으로 inflate
            switch (itemCategory) {
                case ITEM_NORMAL:
                    convertView = inflater.inflate(R.layout.list_item, parent, false);
                    break;
                case ITEM_CHECKABLE:
                    convertView = inflater.inflate(R.layout.list_item_checkbox, parent, false);
                    break;
                case ITEM_SMALL:
                    convertView = inflater.inflate(R.layout.list_item_small, parent, false);
                    break;
                default:
                    break;
            }
        }
        switch (itemCategory) {
            case ITEM_NORMAL:
                folderImage = convertView.findViewById(R.id.sampleImage);
                folderName = convertView.findViewById(R.id.sampleText);
                break;
            case ITEM_CHECKABLE:
                folderImage = convertView.findViewById(R.id.sampleImage_check);
                folderName = convertView.findViewById(R.id.sampleText_check);
                CheckBox checkBox = convertView.findViewById(R.id.checkBox);
                checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked){
                        if(isChecked){
                            path.setDeleteFolderName(listItem.getFolderName(),position);
                        }
                        else{
                            path.setDeleteFolderName(null,position);
                        }
                    }
                });
                break;
            case ITEM_SMALL:
                folderImage = convertView.findViewById(R.id.sampleImage_small);
                folderName = convertView.findViewById(R.id.sampleText_small);
                break;
        }
        folderImage.setImageResource(listItem.getFolderImage());
        folderName.setText(listItem.getFolderName());

        return convertView;
    }
}

