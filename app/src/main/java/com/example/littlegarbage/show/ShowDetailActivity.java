package com.example.littlegarbage.show;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import com.bumptech.glide.Glide;
import com.example.littlegarbage.model.db.GarbageDataBase;
import com.example.littlegarbage.R;
import com.example.littlegarbage.utils.ShotShareUtil;
import com.example.littlegarbage.model.bean.GarbageBean;
import com.example.littlegarbage.search.SearchActivity;

import java.text.DecimalFormat;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ShowDetailActivity extends AppCompatActivity implements ShowDetailActivityContract.View{

    Handler hd;
    String garbage = null;
    GarbageBean.ResultBean.GarbageInfoBean garbageInfoBean = null;
    String citydaima;
    @BindView(R.id.detail_sure) ImageView sureIv;
    @BindView(R.id.detail_share) ImageView shareIv;
    @BindView(R.id.picture_laji) ImageView garbageIv;
    @BindView(R.id.text_status) TextView statusTv;
    @BindView(R.id.text_garbage_name) TextView garbagenametext;
    @BindView(R.id.garbage_name) TextView garbagenameTv;
    @BindView(R.id.text_came_name) TextView camenametext;
    @BindView(R.id.came_name) TextView camenameTv;
    @BindView(R.id.text_city_name) TextView citynametext;
    @BindView(R.id.city_name) TextView citynameTv;
    @BindView(R.id.text_confidence) TextView confidencetext;
    @BindView(R.id.confidence) TextView confidenceTv;
    @BindView(R.id.text_ps_detail) TextView ps_detailtext;
    @BindView(R.id.ps_detail) TextView ps_detailTv;
    @BindView(R.id.just_picture) ImageView justpictureIv;
    @BindView(R.id.text_ps) TextView justpsTv;

    private ShowDetailActivityPresenter showDetailActivityPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_garbage_detail);
        ButterKnife.bind(this);

        GarbageDataBase garbageDataBase= Room.databaseBuilder(
                this,GarbageDataBase.class,"garbage_database").build();


        SearchActivity.open(ShowDetailActivity.this);

        showDetailActivityPresenter = new ShowDetailActivityPresenter(this,garbageDataBase);

        Intent intent = getIntent();
        garbage = intent.getStringExtra("garbage");
        garbageInfoBean = (GarbageBean.ResultBean.GarbageInfoBean) intent.getSerializableExtra("bean");
        citydaima = intent.getStringExtra("citydaima");//?????????????????????

        if (garbage != null) {
            hd = new Handler();

            showDetailActivityPresenter.loadData(this,garbage,citydaima);

        } else if (garbageInfoBean != null) {

            showDetailActivityPresenter.loadData(garbageInfoBean);

        }


    }


    /*???????????????????????????????????????*/
    @OnClick({R.id.detail_sure, R.id.detail_share})
    public void onViewClicked(View view) {

        switch (view.getId()) {

            case R.id.detail_sure:

                showDetailActivityPresenter.clickSure();

                break;

            //??????
            case R.id.detail_share:

                showDetailActivityPresenter.share();

                break;
        }
    }



    /*???????????????garbagebean????????????????????????????????????*/
    private void setDataBeanText(GarbageBean.ResultBean.GarbageInfoBean garbageInfoBean) {

        garbagenameTv.setText(garbageInfoBean.getGarbage_name());
        camenameTv.setText(garbageInfoBean.getCate_name());
        citynameTv.setText(garbageInfoBean.getCity_name());
        confidenceTv.setText(doubleToString(garbageInfoBean.getConfidence()));
        ps_detailTv.setText(garbageInfoBean.getPs());
        statusTv.setText("--??????????????????--");
        justpsTv.setText("???????????????????????????????????????????????????????????????????????????????????????????????????0.7???????????????");

        switch (garbageInfoBean.getCate_name()) {

            case "????????????":
                Glide.with(this).load(R.mipmap.chuyulaji).into(garbageIv);
                break;

            case "?????????":
                Glide.with(this).load(R.mipmap.shilaji).into(garbageIv);
                break;

            case "????????????":
                Glide.with(this).load(R.mipmap.qitalaji).into(garbageIv);
                break;

            case "????????????":
                Glide.with(this).load(R.mipmap.youhailaji).into(garbageIv);
                break;

            case "????????????":
                Glide.with(this).load(R.mipmap.kehuishouwu).into(garbageIv);
                break;

            case "?????????":
                Glide.with(this).load(R.mipmap.ganlaji).into(garbageIv);
                break;

            default:
                Glide.with(this).load(R.mipmap.laji).into(garbageIv);
        }

        Glide.with(this).load(R.mipmap.bg).into(justpictureIv);


        garbagenametext.setText("???????????????");
        camenametext.setText("???????????????");
        citynametext.setText("?????????");
        confidencetext.setText("??????????????????");
        ps_detailtext.setText("???????????????");

    }



    @Override
    public void getDataOnSucceed(GarbageBean garbageBean,String garbage,String garbageString) {


        Thread thread = new Thread(()->{
            Looper.prepare();

            // ??????????????? UI
            hd.post(() -> setDataText(garbageBean));

            Looper.loop();
        });
        thread.start();


    }

    @Override
    public void getDataOnSucceed(GarbageBean.ResultBean.GarbageInfoBean garbageInfoBean) {
        setDataBeanText(garbageInfoBean);
    }

    @Override
    public void getDataOnFailed(String garbage,String garbageString) {

        Thread thread = new Thread(()->{
            Looper.prepare();
            Toast.makeText(this,"??????????????????",Toast.LENGTH_LONG).show();
            Looper.loop();
        });
        thread.start();

    }

    @Override
    public void clickSureFinished() {
        Intent intent = new Intent(this, SearchActivity.class);
        startActivity(intent);
    }

    @Override
    public void shareFinished() {
        // View???????????????
        View getView = this.getWindow().getDecorView();
        getView.setDrawingCacheEnabled(true);
        getView.buildDrawingCache();
        Bitmap b1 = getView.getDrawingCache();

        // ?????????????????????
        Rect frame = new Rect();
        this.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
        int statusBarHeight = frame.top;

        // ?????????????????????
        int width = this.getWindowManager().getDefaultDisplay().getWidth();
        int height = this.getWindowManager().getDefaultDisplay().getHeight();
        // ???????????????
        Bitmap b = Bitmap.createBitmap(b1, 0, statusBarHeight, width, height
                - statusBarHeight);
        getView.destroyDrawingCache();

        ShotShareUtil.shotShare(this, b);//??????????????????
    }

    private void setDataText(GarbageBean finalGb) {

        if (finalGb != null) {

            List<GarbageBean.ResultBean.GarbageInfoBean> garbageInfoBean = finalGb.getResult().getGarbage_info();

            setDataBeanText(garbageInfoBean.get(0));

        }
    }




    /*???confidence??????double????????????????????????????????????*/
    public static String doubleToString(double num) {
        //??????0.00????????????0???#.##??????????????????
        return new DecimalFormat("0.00").format(num);
    }

}
