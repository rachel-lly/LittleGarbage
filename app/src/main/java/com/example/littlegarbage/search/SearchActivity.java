package com.example.littlegarbage.search;

import android.Manifest;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.room.Room;

import com.bumptech.glide.Glide;
import com.example.littlegarbage.show.ShowDetailActivity;
import com.example.littlegarbage.adapter.SearchHistoryAdapter;
import com.example.littlegarbage.model.db.GarbageDataBase;
import com.example.littlegarbage.model.db.GarbageDataDao;
import com.example.littlegarbage.R;
import com.example.littlegarbage.utils.AudioUtil;
import com.example.littlegarbage.utils.PictureUtil;
import com.example.littlegarbage.model.bean.GarbageBean;
import com.example.littlegarbage.moreChoose.MoreChooseActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SearchActivity extends AppCompatActivity implements SearchActivityContract.View {

    @BindView(R.id.garbage_search_autoCompelete)
    AutoCompleteTextView searchnameATV;
    @BindView(R.id.search_sound)
    ImageView soundIv;

    /*???????????????*/
    @BindView(R.id.hot_history_Gridview)
    GridView hot_historyGv;

    /*??????????????????*/
    @BindView(R.id.search_history)
    GridView searchHistoryGv;

    private ArrayAdapter<String> arrayAdapter;

    String Imagename;

    Handler hd;

    /*?????????*/
    public static final int TAKE_PHOTO = 1;
    private Uri imageUri;
    String imgBase;

    /*?????????????????????*/
    public static final int CHOOSE_PHOTO = 2;

    /*?????????*/
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO};
    boolean isFirst = true;//????????????????????????????????????????????????????????????

    private static int REQUEST_PERMISSION_CODE = 3;

    /*??????AudioRecorder??????wav??????*/
    AudioUtil audioUtil = AudioUtil.getInstance();

    String garbage;
    SearchHistoryAdapter historyAdapter;
    List<String> garbagenameList;

    /*????????????*/
    Map<String, Integer> city = new HashMap<>();
    static String cityname = null;
    static String citydaima = null;

    final static String hotSearchHistoryURL = "https://api.tianapi.com/txapi/hotlajifenlei/index?key=2fb9da721d164cdc0a45b990545796fa";
//    final static String imageNameURL = "https://api.zhetaoke.com:10001/api/api_suggest.ashx?appkey=3982f6785fcd4b54a214c69f4c167477";

    final static String hotHistoryURL = "https://api.tianapi.com/txapi/hotlajifenlei/";
    final static String hotHistoryKey = "2fb9da721d164cdc0a45b990545796fa";

    final static String imageNameURL = "https://api.zhetaoke.com:10001/api/";
    final static String imageAppkey = "3982f6785fcd4b54a214c69f4c167477";

    final static List<String> newdata = new ArrayList<>();

    GarbageDataDao garbageDataDao;

    private SearchActivityPresenter searchActivityPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        ButterKnife.bind(this);

        searchActivityPresenter = new SearchActivityPresenter(this);

        GarbageDataBase garbageDataBase = Room.databaseBuilder(
                this, GarbageDataBase.class, "garbage_database").build();
        garbageDataDao= garbageDataBase.getGarbageDataDao();

        //???????????????
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        hd = new Handler();


        Intent intent = getIntent();
        cityname = intent.getStringExtra("city");

        initViews();

        searchActivityPresenter.getHotSearchData(this,hotHistoryURL,hotHistoryKey);


    }

    @OnClick({R.id.shezhi, R.id.garbage_search, R.id.search_sound, R.id.search_takepicture, R.id.search_photo})
    public void onViewClicked(View view) {
        switch (view.getId()) {

            case R.id.shezhi:

                searchActivityPresenter.clickshezhi();

                break;

            case R.id.garbage_search:

                searchActivityPresenter.clickSearch();

                break;

            /*???????????????????????????*/
            case R.id.search_sound:

                open(this);//??????????????????

                if (isFirst) {
                    Glide.with(this).load(R.mipmap.yuyinzanting).into(soundIv);

                    audioUtil.startRecord();
                    audioUtil.recordData();

                    showToastShort(this, "????????????");

                    isFirst = false;
                } else {
                    Glide.with(this).load(R.mipmap.yuyin).into(soundIv);

                    audioUtil.stopRecord();
                    audioUtil.convertWaveFile();
                    showToastShort(this, "??????????????????????????????...");

                    searchActivityPresenter.getSoundData(this,citydaima);

                    isFirst = true;
                }

                break;

            /*??????*/
            case R.id.search_takepicture:

                startTakePicture();

                break;

            /*??????????????????*/
            case R.id.search_photo:

                //??????????????????
                if (ContextCompat.checkSelfPermission
                        (this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                } else {
                    openAlbum();
                }

                break;
        }
    }

    @Override
    public void clickshezhiFinished() {
        Intent intent = new Intent(this, MoreChooseActivity.class);
        startActivity(intent);
    }

    @Override
    public void clickSearchFinished() {
        garbage = searchnameATV.getText().toString();
        if (!TextUtils.isEmpty(garbage)) {
            getTheGarbageMessageToIntent(garbage);//????????????????????????????????????????????????
        } else {
           showToastShort(this, "????????????????????????");
        }
    }

    @Override
    public void getDataOnSucceed(GarbageBean garbageBean) {
        Thread thread = new Thread(()->{
            // ??????????????? UI
            hd.post(() -> getTheGarbageMessage(garbageBean));
        });
        thread.start();
    }

    @Override
    public void getDataOnSucceed(String data) {
        Thread thread = new Thread(()->{
            // ??????????????? UI
            hd.post(() -> setData(data));
        });
        thread.start();

    }

    @Override
    public void getImageDataOnSucceed(String data) {

        Thread thread = new Thread(()->{
            // ??????????????? UI
            hd.post(() -> setImageData(data));
        });
        thread.start();

    }

    @Override
    public void getDataOnFailed() {
        showToastLong(this, "??????????????????");
    }

    void showToastShort(Context context, String message){
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
    void showToastLong(Context context, String message){
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    /*?????????????????????json??????*/
    public void getTheGarbageMessage(GarbageBean garbageBean) {

        List<GarbageBean.ResultBean.GarbageInfoBean> NameList  =
                garbageBean.getResult().garbage_info;
        Double confidence = NameList.get(0).getConfidence();
        int maxindex=0;

        for(int i = 1;i < NameList.size();i++){
            if(confidence<NameList.get(i).getConfidence()){
                confidence=NameList.get(i).getConfidence();
                maxindex=i;
            }
        }

        GarbageBean.ResultBean.GarbageInfoBean gib = NameList.get(maxindex);
        Intent intent = new Intent(this, ShowDetailActivity.class);
        intent.putExtra("bean", gib);//??????????????????bean??????????????????
        intent.putExtra("citydaima", citydaima);
        startActivity(intent);


    }

    /*??????????????????*/
    public static void open(Activity obj) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            for (int i = 0; i < PERMISSIONS_STORAGE.length; i++) {
                if (ActivityCompat.checkSelfPermission(obj,
                        PERMISSIONS_STORAGE[i]) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(obj, PERMISSIONS_STORAGE, REQUEST_PERMISSION_CODE);
                }
            }
        }
    }

    private void startTakePicture() {

        /*??????File?????????????????????????????????*/
        String sdCardDir = Environment.getExternalStorageDirectory() + "/BitmapTest";
        File dirFile = new File(sdCardDir);  //????????????????????????
        if (!dirFile.exists()) {              //?????????????????????????????????????????????
            dirFile.mkdirs();
        }
        File outputImage = new File(sdCardDir, "output_image.png");
        if (outputImage.exists()) {
            outputImage.delete();
        }
        try {
            outputImage.createNewFile();//????????????
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (Build.VERSION.SDK_INT >= 24) {
            imageUri = FileProvider.getUriForFile
                    (this, "com.example.littlegarbage.fileprovider", outputImage);
        } else {
            imageUri = Uri.fromFile(outputImage);
        }
        /*??????????????????*/
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, TAKE_PHOTO);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case TAKE_PHOTO:
                if (resultCode == RESULT_OK) {

                    try {
                        Bitmap bitmap = BitmapFactory.decodeStream
                                (getContentResolver().openInputStream(imageUri));

                        getThePictureName(bitmap);

                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                break;

            case CHOOSE_PHOTO:
                if (resultCode == RESULT_OK) {
                    //???????????????????????????
                    if (Build.VERSION.SDK_INT >= 19) {
                        //4.4???????????????
                        handleImageOnKitKat(data);
                    } else {
                        //4.4????????????
                        handleImageBeforeKitKat(data);
                    }
                }

            default:
                break;
        }
    }

    private void handleImageOnKitKat(Intent data) {
        String imagePath = null;
        Uri uri = data.getData();
        if (DocumentsContract.isDocumentUri(this, uri)) {
            //?????????document?????????Uri????????????document id??????
            String docId = DocumentsContract.getDocumentId(uri);
            if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
                String id = docId.split(":")[1];//????????????????????????id
                String selection = MediaStore.Images.Media._ID + "=" + id;
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
            } else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                Uri contentUri = ContentUris.withAppendedId(Uri.parse(
                        "content://downloads/public_downloads"), Long.valueOf(docId));
                imagePath = getImagePath(contentUri, null);
            } else if ("content".equalsIgnoreCase(uri.getScheme())) {
                //?????????content?????????uri??????????????????????????????
                imagePath = getImagePath(uri, null);
            } else if ("file".equalsIgnoreCase(uri.getScheme())) {
                //?????????file?????????uri?????????????????????????????????
                imagePath = uri.getPath();
            }
            displayImage(imagePath);
        }
    }

    private void handleImageBeforeKitKat(Intent data) {
        Uri uri = data.getData();
        String imagePath = getImagePath(uri, null);
        displayImage(imagePath);
    }

    private void getThePictureName(Bitmap bitmap) {

        Bitmap bm = PictureUtil.compressScale(bitmap);//????????????

        imgBase = PictureUtil.bitmaptoString(bm);//???????????????Base64??????
        showToastShort(this, "???????????????...");
        searchActivityPresenter.getPictureData(this,imgBase, citydaima);

    }

    /*??????????????????*/
    private String getImagePath(Uri uri, String selection) {
        String path = null;
        //??????Uri???selection?????????????????????????????????
        Cursor cursor = getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }

    /*????????????????????????????????????????????????json??????*/
    public void setData(String data) {

        if (data != null) {
            JSONObject joname = null;
            try {
                joname = new JSONObject(data);
                if (joname.getInt("code") == 200) {
                    JSONArray listArray = joname.getJSONArray("newslist");
                    for (int i = 0; i < listArray.length(); i++) {
                        JSONObject jsonArray = listArray.getJSONObject(i);
                        String name = jsonArray.getString("name");
                        if (name.length() < 5 && newdata.size() < 16 && !newdata.contains(name)) {
                            newdata.add(name);
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        arrayAdapter = new ArrayAdapter<>(this, R.layout.item_hotgarbage, newdata);
        hot_historyGv.setAdapter(arrayAdapter);

        /*???????????????????????????*/
        hot_historyGv.setOnItemClickListener((parent, view, position, id) -> {
            garbage = newdata.get(position);
            getTheGarbageMessageToIntent(garbage);
        });

    }

    /*????????????garbage???????????????*/
    private void getTheGarbageMessageToIntent(String garbage) {

        Intent intent = new Intent(this, ShowDetailActivity.class);
        intent.putExtra("garbage", garbage);
        intent.putExtra("citydaima", citydaima);
        startActivity(intent);

    }


    /*?????????????????????*/
    private void setImageData(String finals) {

        List<String> ImageNameList = new ArrayList<>();
        if (finals != null) {
            JSONObject joname = null;
            try {
                joname = new JSONObject(finals);

                JSONArray listArray = joname.getJSONArray("result");
                if(listArray!=null){
                    for (int i = 0; i < listArray.length(); i++) {
                        JSONArray NameArray = listArray.getJSONArray(i);
                        String name = NameArray.getString(0);
                        ImageNameList.add(name);
                    }
                }


            } catch (JSONException e) {
                e.printStackTrace();
            }

            ArrayAdapter<String> atvArrayAdapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_dropdown_item_1line, ImageNameList);

            searchnameATV.setAdapter(atvArrayAdapter);
            atvArrayAdapter.notifyDataSetChanged();

        }

    }

    private void initViews() {

        Thread thread = new Thread(() -> {
            garbagenameList = garbageDataDao.queryAllGarbageName();
            if (garbagenameList != null) {
                historyAdapter = new SearchHistoryAdapter(this, garbagenameList);
                searchHistoryGv.setAdapter(historyAdapter);
                searchHistoryGv.setOnItemClickListener((arg0, v, index, arg3) -> {
                    String garbage = (String) historyAdapter.getItem(index);
                    getTheGarbageMessageToIntent(garbage);
                });
            }
        });
        thread.start();

        //310000(?????????)???330200(?????????)???610100(?????????)???440300(?????????)????????????(110000)
        //????????????api???????????????
        city.put("??????", 310000);
        city.put("??????", 330200);
        city.put("??????", 610100);
        city.put("??????", 440300);
        city.put("??????", 110000);

        if (cityname != null) {//?????????????????????????????????
            citydaima = String.valueOf(city.get(cityname));//????????????id
        }


        /*?????????AutoCompeleteTextView??????????????????*/
        searchnameATV.setThreshold(1);//??????????????????????????????????????????


        /*?????????????????????  ??????????????????*/
        searchnameATV.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                String name = searchnameATV.getText().toString();

                try {
                    Imagename = URLEncoder.encode(name, "UTF-8");

                   searchActivityPresenter.getImageData(SearchActivity.this,imageNameURL,imageAppkey,Imagename);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

            }

        });

    }

    private void openAlbum() {

        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        startActivityForResult(intent, CHOOSE_PHOTO);//????????????

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openAlbum();
                } else {
                    showToastLong(this, "????????????????????????");
                }
                break;

            default:
                break;
        }
    }


    private void displayImage(String imagePath) {
        if (imagePath != null) {
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            getThePictureName(bitmap);

        } else {
           showToastLong(this, "??????????????????");
        }
    }


}
