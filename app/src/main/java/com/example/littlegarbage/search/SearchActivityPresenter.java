package com.example.littlegarbage.search;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Looper;
import android.widget.Toast;

import com.example.littlegarbage.model.bean.GarbageBean;
import com.example.littlegarbage.utils.GetHttpData;
import com.example.littlegarbage.utils.HttpUtil;
import com.example.littlegarbage.utils.JsonParser;

import org.json.JSONException;

import java.net.MalformedURLException;


public class SearchActivityPresenter implements SearchActivityContract.Presenter {

    SearchActivityContract.View mView;

    public SearchActivityPresenter(SearchActivityContract.View mView) {
        this.mView = mView;
    }


    @Override
    public void clickshezhi() {
        mView.clickshezhiFinished();
    }

    @Override
    public void clickSearch() {
        mView.clickSearchFinished();
    }

    @Override
    public void getHotSearchData(String hotSearchHistoryURL) {
        // 启用网络线程
        HttpThreadToGetData ht = new HttpThreadToGetData(hotSearchHistoryURL);
        ht.start();
    }

    @Override
    public void getImageData(String imageUrl) {
        //网络请求
        HttpThreadToGetImageData httpThreadToGetImageData = new HttpThreadToGetImageData(imageUrl);
        httpThreadToGetImageData.start();
    }

    @Override
    public void getSoundData(Context context,String citydaima) {

        HttpThreadToGetSoundName getsoundName = new HttpThreadToGetSoundName(context,citydaima);
        getsoundName.start();

    }

    @Override
    public void getPictureData(String imgBase,String citydaima) {
        // 启用网络线程
        HttpThreadToGetPictureName ht = new HttpThreadToGetPictureName(imgBase,citydaima);
        ht.start();
    }



    /*获取热门搜索数据*/
    public class HttpThreadToGetData extends Thread {

        String hotSearchHistoryURL;

        public HttpThreadToGetData(String hotSearchHistoryURL) {
            this.hotSearchHistoryURL = hotSearchHistoryURL;
        }

        @Override
        public void run() {
            super.run();
            try {
                String data = GetHttpData.GetHotData(hotSearchHistoryURL);

                mView.getDataOnSucceed(data);


            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
    }

    /*获取联想词数据*/
    public class HttpThreadToGetImageData extends Thread {

        String imageUrl;

        public HttpThreadToGetImageData(String imageUrl) {
            this.imageUrl = imageUrl;
        }

        @Override
        public void run() {
            super.run();
            try {
                String imageData = GetHttpData.GetHotData(imageUrl);
                mView.getImageDataOnSucceed(imageData);


            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
    }

    /*网络请求，获取语音识别的数据*/
    public class HttpThreadToGetSoundName extends Thread {

        Context context;
        String citydaima;

        public HttpThreadToGetSoundName(Context context,String citydaima) {
            this.context = context;
            this.citydaima=citydaima;
        }

        @Override
        public void run() {
            super.run();
            Looper.prepare();
            // 城市代码
            String garbageString = null;
            try {

                String model = Build.MODEL;
                String version_release = Build.VERSION.RELEASE;
                Integer packagecode = packageCode(context);

                garbageString = HttpUtil.sendOkHttpSoundRequest(model, version_release, packagecode, citydaima);

            } catch (JSONException | MalformedURLException e) {
                e.printStackTrace();
            }

            JsonParser jp = new JsonParser();
            GarbageBean garbageBean = jp.GarbageParse(garbageString);

            if (garbageBean!=null) {
                mView.getDataOnSucceed(garbageBean);

            } else {
                mView.getDataOnFailed();
            }
            Looper.loop();
        }
    }

    /*获取客户端版本号*/
    public static int packageCode(Context context) {
        PackageManager manager = context.getPackageManager();
        int code = 0;
        try {
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            code = info.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return code;
    }

    /*网络请求，获取图像识别的数据*/
    public class HttpThreadToGetPictureName extends Thread {

       String imgBase;
       String citydaima;

        public HttpThreadToGetPictureName(String imgBase, String citydaima) {
            this.imgBase = imgBase;
            this.citydaima = citydaima;
        }

        @Override
        public void run() {
            super.run();
            Looper.prepare();
            // 城市代码
            String garbageString = null;
            try {

                garbageString = HttpUtil.sendOkHttpPictureRequest(imgBase, citydaima);

            } catch (JSONException | MalformedURLException e) {
                e.printStackTrace();
            }

            JsonParser jp = new JsonParser();
            GarbageBean garbageBean = jp.GarbageParse(garbageString);

            if (garbageBean!=null) {
                mView.getDataOnSucceed(garbageBean);

            } else {
                mView.getDataOnFailed();
            }
            Looper.loop();
        }
    }






}
