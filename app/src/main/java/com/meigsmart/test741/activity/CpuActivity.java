package com.meigsmart.test741.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;

import com.meigsmart.test741.MyApplication;
import com.meigsmart.test741.R;
import com.meigsmart.test741.db.TypeModel;
import com.meigsmart.test741.service.CpuService1;
import com.meigsmart.test741.service.CpuService2;
import com.meigsmart.test741.service.CpuService3;
import com.meigsmart.test741.util.CpuMessage;
import com.meigsmart.test741.util.DateUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.NumberFormat;

public class CpuActivity extends BaseActivity implements CpuMessage.OnCallBack{
    private TextView mTitle;
    private TextView mResult;

    private int mBroadType = 0;
    private TypeModel model;
    private Intent intent1;
    private Intent intent2;
    private Intent intent3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cpu);
        mTitle = (TextView)findViewById(R.id.title);
        mResult = (TextView)findViewById(R.id.result);
        mTitle.setText("CPU TEST");

        mBroadType = getIntent().getIntExtra("broadType",0);

        if (mBroadType == 1){
            String type = getIntent().getStringExtra("type");
            int time = getIntent().getIntExtra("time",0);

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
            MyApplication.getInstance().mDb.update(model.getType(),0,2);
        }
        this.finish();
    }

    @Override
    protected void success(){
        if (model != null){
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
        stopService();
        CpuMessage.getInstance().stop();
    }

    @Override
    public void onBackPressed() {
        exit();
        this.finish();
        super.onBackPressed();
    }

    private void init(){
        startService();
        CpuMessage.getInstance().init(this,100L);
        CpuMessage.getInstance().start();
        CpuMessage.getInstance().setCallBack(this);
    }

    public void startService() {
        intent1 = new Intent(this, CpuService1.class);
        startService(intent1);

        intent2 = new Intent(this, CpuService2.class);
        startService(intent2);

        intent3 = new Intent(this, CpuService3.class);
        startService(intent3);
    }

    private void stopService(){
        stopService(intent1);
        stopService(intent2);
        stopService(intent3);
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

    @Override
    public void run(double cpu) {
        Message msg = mHandler.obtainMessage();
        msg.what = 1001;
        msg.obj = cpu;
        mHandler.sendMessage(msg);
    }

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 1001:
                    NumberFormat nf = NumberFormat.getNumberInstance();
                    nf.setMaximumFractionDigits(2);
                    mResult.setText("CPU使用率："+nf.format(Double.parseDouble(msg.obj.toString()))+"%");
                    if (model!=null){
                        if (DateUtil.getTimeInterval(DateUtil.getCurrentDate(), model.getStartTime()) >= model.getAllTime() * 60) {
                            success();
                            stopService();
                            CpuActivity.this.finish();
                        }
                    }
                    break;
            }
        }
    };
}
