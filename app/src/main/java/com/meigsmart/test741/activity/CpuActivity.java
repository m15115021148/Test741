package com.meigsmart.test741.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import com.meigsmart.test741.MyApplication;
import com.meigsmart.test741.R;
import com.meigsmart.test741.config.RequestCode;
import com.meigsmart.test741.db.TypeModel;
import com.meigsmart.test741.service.CpuService1;
import com.meigsmart.test741.util.DateUtil;
import com.meigsmart.test741.util.PreferencesUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.text.NumberFormat;

public class CpuActivity extends BaseActivity implements Runnable{
    private TextView mTitle;
    private TextView mOver;
    private TextView mResult;

    private int mBroadType = 0;
    private TypeModel model;
    private Intent intent1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cpu);
        mTitle = findViewById(R.id.include).findViewById(R.id.title);
        mOver = findViewById(R.id.include).findViewById(R.id.over);
        mOver.setVisibility(View.VISIBLE);
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
            init();
        }

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
                        stopService();
                        mHandler.removeCallbacks(CpuActivity.this);
                        mHandler.removeMessages(1001);
                        mHandler.removeMessages(1002);
                        CpuActivity.this.finish();
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
        this.finish();
    }

    @Override
    protected void success() {
        if (model != null) {
            PreferencesUtil.setStringData(this,"type", RequestCode.ANDROID_REBOOT);
            MyApplication.getInstance().mDb.update(model.getType(), 0, 1);
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
        stopService();
        mHandler.removeCallbacks(CpuActivity.this);
        mHandler.removeMessages(1001);
        mHandler.removeMessages(1002);
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

    private void init() {
        startService();
        calculateCpuUsage();
    }

    public void startService() {
        intent1 = new Intent(this, CpuService1.class);
        startService(intent1);
    }

    private void stopService() {
        stopService(intent1);
    }

    private int getProcessCpuRate() {

        StringBuilder tv = new StringBuilder();
        int rate = 0;

        try {
            String Result;
            Process p;
            p = Runtime.getRuntime().exec("top -n 1");

            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            while ((Result = br.readLine()) != null) {
                if (Result.trim().length() < 1) {
                    continue;
                } else {
                    String[] CPUusr = Result.split("%");
                    tv.append("USER:" + CPUusr[0] + "\n");
                    String[] CPUusage = CPUusr[0].split("User");
                    String[] SYSusage = CPUusr[1].split("System");
                    tv.append("CPU:" + CPUusage[1].trim() + " length:" + CPUusage[1].trim().length() + "\n");
                    tv.append("SYS:" + SYSusage[1].trim() + " length:" + SYSusage[1].trim().length() + "\n");

                    rate = Integer.parseInt(CPUusage[1].trim()) + Integer.parseInt(SYSusage[1].trim());
                    break;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return rate;
    }


    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1001:
                    NumberFormat nf = NumberFormat.getNumberInstance();
                    nf.setMaximumFractionDigits(2);
                    mHandler.sendEmptyMessage(1002);
                    mResult.setText("CPU使用率：" + nf.format(getProcessCpuRate() )+ "%");

                    if (model != null) {
                        if (DateUtil.getTimeInterval(DateUtil.getCurrentDate(), model.getStartTime()) >= model.getAllTime() * 60) {
                            success();
                            stopService();
                            mHandler.removeCallbacks(CpuActivity.this);
                            mHandler.removeMessages(1001);
                            mHandler.removeMessages(1002);
                            CpuActivity.this.finish();
                        }
                    }
                    mHandler.postDelayed(CpuActivity.this,1000);
                    break;
                case 1002:
                    useCpu();
                    break;
            }
        }
    };


    private void calculateCpuUsage() {
        mHandler.post(this);
    }

    private float readUsage() {
        try {
            RandomAccessFile randomaccessfile;
            long l;
            long l1;
            long l2;
            randomaccessfile = new RandomAccessFile("/proc/stat", "r");
            String as[] = randomaccessfile.readLine().split(" ");
            l = Long.parseLong(as[5]);
            l1 = Long.parseLong(as[2]) + Long.parseLong(as[3]) + Long.parseLong(as[4]) + Long.parseLong(as[6]) + Long.parseLong(as[7]);
            l2 = Long.parseLong(as[8]);
            float f;
            long l3 = l1 + l2;
            String s;
            String as1[];
            long l4;
            long l5;

            Thread.sleep(100L);

            randomaccessfile.seek(0L);
            s = randomaccessfile.readLine();
            randomaccessfile.close();
            as1 = s.split(" ");
            l4 = Long.parseLong(as1[5]);
            l5 = Long.parseLong(as1[2]) + Long.parseLong(as1[3]) + Long.parseLong(as1[4]) + Long.parseLong(as1[6]) + Long.parseLong(as1[7]) + Long.parseLong(as1[8]);
            f = (100L * (l5 - l3)) / ((l5 + l4) - (l3 + l));
            return f;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    private void useCpu() {
        (new Thread(new Runnable() {

            public void run() {
//                int i = 150000000;
                int i = 999999999;
                do {
                    if (i == 1) {
                        runOnUiThread(new Runnable() {
                                          public void run() {
                                              mHandler.sendEmptyMessageDelayed(1002, 500L);
                                          }
                                      }
                        );
                        return;
                    }
                    i--;
                } while (true);
            }
        }, "UseCpu")).start();
    }

    @Override
    public void run() {
        mHandler.sendEmptyMessage(1001);
    }
}
