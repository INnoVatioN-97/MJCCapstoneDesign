package com.example.ocr_api_test;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Camera;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ocr_api_test.camera.CameraActivity;

import com.example.ocr_api_test.camera.Preview;
import com.example.ocr_api_test.db.DBHelper;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequest;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
//앱의 주요기능인 Google OCR API. 스레드가 아닌 함수로 작동하게 했습니다
public class OCR {

    private Context mContext;
    private static final String CLOUD_VISION_API_KEY = BuildConfig.API_KEY; //Google Vision API키
    private static final String ANDROID_CERT_HEADER = "X-Android-Cert";
    private static final String ANDROID_PACKAGE_HEADER = "X-Android-Package";
    private static final int MAX_DIMENSION = 1200;
    private static final String TAG = OCR.class.getSimpleName();
    MainActivity mainInstance = new MainActivity();
    StringManager path = new StringManager();
    StringBuilder message;

    //DB
    DBHelper helper;
    SQLiteDatabase db;
    Cursor cursor;

    public OCR(Context context){
        this.mContext = context;
        helper = new DBHelper(mContext);
        db = helper.getWritableDatabase();
    }

    public String doOCR(byte[] bytes){
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
        Bitmap bitmap2;
               bitmap2 = scaleBitmapDown(bitmap, MAX_DIMENSION);
        callCloudVision(bitmap2);
        return message.toString();
    }

    private Vision.Images.Annotate prepareAnnotationRequest(Bitmap bitmap) throws IOException {
        HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

        VisionRequestInitializer requestInitializer =
                new VisionRequestInitializer(CLOUD_VISION_API_KEY) {
                    /**
                     * We override this so we can inject important identifying fields into the HTTP
                     * headers. This enables use of a restricted cloud platform API key.
                     */
                    @Override
                    protected void initializeVisionRequest(VisionRequest<?> visionRequest)
                            throws IOException {
                        super.initializeVisionRequest(visionRequest);

                        String packageName = mContext.getPackageName();
                        visionRequest.getRequestHeaders().set(ANDROID_PACKAGE_HEADER, packageName);

                        String sig = PackageManagerUtils.getSignature(mContext.getPackageManager(), packageName);

                        visionRequest.getRequestHeaders().set(ANDROID_CERT_HEADER, sig);
                    }
                };

        Vision.Builder builder = new Vision.Builder(httpTransport, jsonFactory, null);
        builder.setVisionRequestInitializer(requestInitializer);

        Vision vision = builder.build();

        BatchAnnotateImagesRequest batchAnnotateImagesRequest =
                new BatchAnnotateImagesRequest();
        batchAnnotateImagesRequest.setRequests(new ArrayList<AnnotateImageRequest>() {{
            AnnotateImageRequest annotateImageRequest = new AnnotateImageRequest();

            // Add the image
            com.google.api.services.vision.v1.model.Image base64EncodedImage = new com.google.api.services.vision.v1.model.Image();
            // Convert the bitmap to a JPEG
            // Just in case it's a format that Android understands but Cloud Vision
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream);
            byte[] imageBytes = byteArrayOutputStream.toByteArray();

            // Base64 encode the JPEG
            base64EncodedImage.encodeContent(imageBytes);
            annotateImageRequest.setImage(base64EncodedImage);

            // add the features we want
            annotateImageRequest.setFeatures(new ArrayList<Feature>() {{
                Feature textDetection = new Feature();
                textDetection.setType("TEXT_DETECTION");
                textDetection.setMaxResults(10);
                add(textDetection);
            }});

            // Add the list of one thing to the request
            add(annotateImageRequest);
        }});

        Vision.Images.Annotate annotateRequest =
                vision.images().annotate(batchAnnotateImagesRequest);
        // Due to a bug: requests to Vision API containing large images fail when GZipped.
        annotateRequest.setDisableGZipContent(true);
        Log.d(TAG, "created Cloud Vision request object, sending request");

        return annotateRequest;
    }

    private String LabelDetectionTask(Vision.Images.Annotate annotate) throws IOException{
        BatchAnnotateImagesResponse response = annotate.execute();
        return convertResponseToString(response);
    }

    public void callCloudVision(final Bitmap bitmap) {
        // Switch text to loading
        // Do the real work in an async task, because we need to use the network anyway
        // 검출한 폴더명을 반영해 사진파일을 만들기 위해 스레드로 구현하지 않았습니다
        try {
            LabelDetectionTask(prepareAnnotationRequest(bitmap));
        } catch (IOException e) {
            Log.d(TAG, "failed to make API request because of other IOException " +
                    e.getMessage());
        }
    }

    public Bitmap scaleBitmapDown(Bitmap bitmap, int maxDimension) {

        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();
        int resizedWidth = maxDimension;
        int resizedHeight = maxDimension;

        if (originalHeight > originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = (int) (resizedHeight * (float) originalWidth / (float) originalHeight);
        } else if (originalWidth > originalHeight) {
            resizedWidth = maxDimension;
            resizedHeight = (int) (resizedWidth * (float) originalHeight / (float) originalWidth);
        } else if (originalHeight == originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = maxDimension;
        }
        return Bitmap.createScaledBitmap(bitmap, resizedWidth, resizedHeight, false);
    }
    //검출한 문자열들을 폴더별로 키워드들과 비교해, 문자열과 키워드가 가장 많이 매칭된 폴더를 저장경로로 설정합니다
    private String convertResponseToString(BatchAnnotateImagesResponse response) {
        message = new StringBuilder("\n\n");
        List<EntityAnnotation> labels = response.getResponses().get(0).getTextAnnotations();
        setKeywords();

        if (labels != null) {
            int subjectCount = path.KEYWORD.length;
            int cnt[] = new int[subjectCount];
            for (EntityAnnotation label : labels) {
                for (int i = 0; i < subjectCount; i++) {
                    for (int j = 0; j < path.KEYWORD[i].length; j++) {
                        //사진속에서 검출된 문자들과 KEYWORD 배열의 각 원소(과목별 연관 키워드)를 비교해 각 과목당의 일치횟수를 계산한다.
                        if (label.containsValue(path.KEYWORD[i][j])) cnt[i]++;
                    }
                }
            }
            int max = 0, index = 0, max_2nd = 0; //키워드가 가장 많이 검출된 변수, 그 변수의 인덱스, 두번째로 많이 검출된 변수 선언
            for (int i = 0; i < cnt.length; i++) {
                if (cnt[i] > max) {
                    max_2nd = max; //2번째로 큰 값을 저장하는 변수
                    max = cnt[i];
                    index = i;
                } else if (cnt[i] == max)
                    max_2nd = max;
            }
            if (max != max_2nd) {
                String ocrSavePath = mainInstance.getSaveFolder(path.SUBJECTS[index]).getAbsolutePath();
                path.setFullSavePath(ocrSavePath);
                path.setSavePathFolderName(path.SUBJECTS[index]);

                message.replace(0, 0, "이 과목은 [" + path.SUBJECTS[index] + "]로 감지되었습니다\n"
                        + "사진의 저장경로를 [" + path.getRecentFolderName() + "]로 설정합니다");

            } else message.replace(0, 0, "해당 사진과 맞는 폴더를 찾아내지 못했습니다\n다른 사진으로 시도해 보세요 \n\n"
                    + debug(cnt, max, max_2nd));

        } else message.append("nothing");

        return message.toString();
    }

    private String debug(int[] cnt, int max, int max_2nd) {
        String str = "";
        for (int i = 0; i < path.SUBJECTS.length; i++) {
            if (path.SUBJECTS[i] == null) continue;
            str += ("[" + path.SUBJECTS[i] + "] = " + cnt[i] + "개 일치\n");
        }
//        str += ("max = " + max + ", max_2nd = " + max_2nd);
        return str;
    }
    //SQLite Keyword 테이블에서 키워드를 가져와서 StringManager의 KEYWORD 배열을 초기화
    public void setKeywords() {
        cursor = db.rawQuery("Select * from subject;", null);
        path.KEYWORD = new String[cursor.getCount()][20];
        cursor = db.rawQuery("select * from keyword", null); //일단 모든 keyword 값을 가져오기.
        cursor.moveToFirst();

        for (int i = 0; i < path.SUBJECTS.length; i++) {
            if (path.SUBJECTS[i] == null) continue; //추가된 과목이 해당 인덱스에 없으면 continue
            //SUBJECTS[i]과목의 키워드를 커서에 가져온다.
            cursor = db.rawQuery("select keywordName from keyword where subjectName='" + path.SUBJECTS[i] + "'", null);
            cursor.moveToFirst();
            for (int j = 0; j < cursor.getCount(); j++) {
                path.KEYWORD[i][j] = cursor.getString(0);
                System.out.println("\nKEYWORD[" + i + "][" + j + "] =" + path.KEYWORD[i][j]);
                cursor.moveToNext();
            }
        }
    }

}
