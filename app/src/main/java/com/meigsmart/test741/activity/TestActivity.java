package com.meigsmart.test741.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.meigsmart.test741.MyApplication;
import com.meigsmart.test741.R;
import com.meigsmart.test741.config.RequestCode;
import com.meigsmart.test741.db.TypeModel;
import com.meigsmart.test741.util.DateUtil;
import com.meigsmart.test741.util.PreferencesUtil;

import java.util.Timer;
import java.util.TimerTask;

public class TestActivity extends BaseActivity {
    private TextView mTitle;
    private LinearLayout mLayout;

    private int mBroadType = 0;
    private int currNum = 0;

    private TimerTask timerTask;//定时任务
    private Timer timer;//定时器
    private TypeModel model;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        mTitle = (TextView) findViewById(R.id.title);
        mLayout = (LinearLayout) findViewById(R.id.layout);
        mTitle.setText("741测试");
        mBroadType = getIntent().getIntExtra("broadType",0);

        if (mBroadType == 1){
            String type = getIntent().getStringExtra("type");
            int time = Integer.parseInt(PreferencesUtil.getStringData(this,"time"));

            MyApplication.getInstance().mDb.delete(type);

            model = new TypeModel();
            model.setType(type);
            model.setAllTime(time);
            model.setStartTime(DateUtil.getCurrentDate());
            model.setFilepath("");
            model.setIsRun(1);
            model.setIsPass(0);

            MyApplication.getInstance().mDb.addData(model);

            init();
        }

    }

    @Override
    protected void error(){
        if (model != null){
            PreferencesUtil.setStringData(this,"type", RequestCode.ANDROID_ERROR);
            MyApplication.getInstance().mDb.update(model.getType(),0,2);
        }
        this.finish();
    }

    @Override
    protected void success(){
        if (model != null){
            PreferencesUtil.setStringData(this,"type", RequestCode.ANDROID_SUCCESS);
            MyApplication.getInstance().mDb.update(model.getType(),0,1);
        }
    }

    @Override
    protected void exit(){
        if (model != null){
            MyApplication.getInstance().mDb.update(model.getType(),0,0);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeMessages(1001);
        if (timerTask!=null)timerTask.cancel();
        if (timer!=null)timer.cancel();
    }

    @Override
    public void onBackPressed() {
        exit();
        this.finish();
        super.onBackPressed();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(false);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void init(){
        mTitle.setVisibility(View.GONE);
        initLcd();
    }

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 1001:
                    currNum ++;
                    if (model!=null && DateUtil.getTimeInterval(DateUtil.getCurrentDate(),model.getStartTime())>=model.getAllTime()*60){
                        mHandler.removeMessages(1001);
                        if (timerTask!=null)timerTask.cancel();
                        if (timer!=null)timer.cancel();
                        mLayout.setBackgroundColor(Color.WHITE);
                        success();
                        TestActivity.this.finish();
                        break;
                    }
                    if (currNum == 1){
                        mLayout.setBackgroundColor(Color.MAGENTA);
                    }
                    if (currNum == 2){
                        mLayout.setBackgroundColor(Color.RED);
                    }
                    if (currNum == 3){
                        mLayout.setBackgroundColor(Color.BLUE);
                    }
                    if (currNum == 4){
                        mLayout.setBackgroundColor(Color.GREEN);
                    }
                    if (currNum == 5){
                        currNum =0;
                        mLayout.setBackgroundColor(Color.YELLOW);
                    }
                    break;
                default:
                    break;
            }
        }
    };

    private void initLcd(){
        timerTask = new TimerTask() {
            @Override
            public void run() {
                mHandler.sendEmptyMessage(1001);
            }
        };
        timer = new Timer();
        timer.schedule(timerTask, 0, 2000);
    }

}
