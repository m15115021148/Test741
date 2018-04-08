package com.meigsmart.test741.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import com.meigsmart.test741.MyApplication;
import com.meigsmart.test741.R;
import com.meigsmart.test741.config.RequestCode;
import com.meigsmart.test741.cpuservice.CpuTest;
import com.meigsmart.test741.db.TypeModel;
import com.meigsmart.test741.cpuservice.CpuService1;
import com.meigsmart.test741.util.CleanMessageUtil;
import com.meigsmart.test741.util.DateUtil;
import com.meigsmart.test741.util.PreferencesUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;

public class CpuActivity extends BaseActivity{
    private TextView mTitle;
    private TextView mOver;
    private TextView mResult;
    private TextView mExit;

    private int mBroadType = 0;
    private TypeModel model;

    private CpuTest mCpuTest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cpu);
        mTitle = findViewById(R.id.include).findViewById(R.id.title);
        mOver = findViewById(R.id.include).findViewById(R.id.over);
        mOver.setVisibility(View.VISIBLE);
        mExit = findViewById(R.id.include).findViewById(R.id.exit);
        mExit.setVisibility(View.VISIBLE);
        mResult = (TextView) findViewById(R.id.result);
        mTitle.setText("CPU TEST");

        mBroadType = getIntent().getIntExtra("broadType", 0);

        if (mBroadType == 1) {
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
        }
        init();

        mOver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(CpuActivity.this);
                builder.setTitle("提示");
                builder.setMessage("是否结束测试？");
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        success();
                        deInit();
                    }
                });
                builder.setNegativeButton("取消",null);
                builder.create().show();
            }
        });

        mExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(CpuActivity.this);
                builder.setTitle("提示");
                builder.setMessage("是否退出整个测试，重新选择？");
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        MyApplication.getInstance().mDb.deleteAll();
                        PreferencesUtil.isFristLogin(CpuActivity.this,"onClickStart",false);
                        PreferencesUtil.isFristLogin(CpuActivity.this,"first",false);
                        PreferencesUtil.setStringData(CpuActivity.this,"type","");

                        CleanMessageUtil.cleanApplicationData(MyApplication.getInstance().getApplicationContext());

                        deInit();

                        //退出所有的activity
                        Intent intent = new Intent();
                        intent.setAction(BaseActivity.TAG_ESC_ACTIVITY);
                        sendBroadcast(intent);
                        finish();
                    }
                });
                builder.setNegativeButton("取消",null);
                builder.create().show();
            }
        });

    }

    @Override
    protected void error() {
        if (model != null) {
            PreferencesUtil.setStringData(this,"type", RequestCode.ANDROID_ERROR);
            MyApplication.getInstance().mDb.update(model.getType(), 0, 2);
        }
        setResult(1001);
        this.finish();
    }

    @Override
    protected void success() {
        if (model != null) {
            PreferencesUtil.setStringData(this,"type", RequestCode.ANDROID_REBOOT);
            MyApplication.getInstance().mDb.update(model.getType(), 0, 1);
            setResult(1001);
        }
    }

    @Override
    protected void exit() {
        if (model != null) {
            MyApplication.getInstance().mDb.update(model.getType(), 0, 0);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        deInit();
    }

    private void deInit(){
        if (this.mCpuTest != null) {
            this.mCpuTest.stop();
        }
        mHandler.removeMessages(1001);
        mHandler.removeMessages(1002);
        CpuActivity.this.finish();
    }

    private void init(){
        this.mCpuTest = new CpuTest(this,mHandler);
        this.mCpuTest.start();
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

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1001:
                    calculateCpuUsage();
                    if (model != null) {
                        if (DateUtil.getTimeInterval(DateUtil.getCurrentDate(), model.getStartTime()) >= model.getAllTime() * 60) {
                            success();
                            deInit();
                        }
                    }
                    break;
                case 1002:
                    useCpu();
                    break;
            }
        }
    };

    private void calculateCpuUsage() {
        new Thread(new Runnable() {
            public void run() {
                float f = readUsage();
                String str = "Cpu Usage : " + f + "%";
                updateCpuInfoText(str);
                mHandler.sendEmptyMessageDelayed(1001, 1000L);
            }
        }, "CalCpu").start();
    }

    private float readUsage() {
        try {
            RandomAccessFile localObject1 = new RandomAccessFile("/proc/stat", "r");
            String[] localObject2 = localObject1.readLine().split(" ");
            long l1 = Long.parseLong(localObject2[5]);
            long l2 = Long.parseLong(localObject2[2]) + Long.parseLong(localObject2[3]) + Long.parseLong(localObject2[4]) + Long.parseLong(localObject2[6]) + Long.parseLong(localObject2[7]) + Long.parseLong(localObject2[8]);
            try {
                Thread.sleep(100L);
                localObject1.seek(0L);
                String str = localObject1.readLine();
                localObject1.close();
                String[] str1 = str.split(" ");
                long l3 = Long.parseLong(str1[5]);
                long l4 = Long.parseLong(str1[2]) + Long.parseLong(str1[3]) + Long.parseLong(str1[4]) + Long.parseLong(str1[6]) + Long.parseLong(str1[7]) + Long.parseLong(str1[8]);
                return (float) (Long.valueOf(100L * (l4 - l2) / (l4 + l3 - (l2 + l1))).longValue());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return 0.0F;
        } catch (Exception localException1) {
            localException1.printStackTrace();
        }
        return 0.0F;
    }

    private void updateCpuInfoText(String paramString) {
        Intent localIntent = new Intent(this, CpuService1.class);
        localIntent.putExtra("update", true);
        localIntent.putExtra("level", paramString);
        startService(localIntent);
    }

    private void useCpu() {
        new Thread(new Runnable() {
            public void run() {
                int i = 500000000;
                for (; ; ) {
                    if (i == 1) {
                        mHandler.sendEmptyMessageDelayed(1002, 500L);
                        break;
                    }
                    i -= 1;
                }
            }
        }, "UseCpu").start();
    }

}
