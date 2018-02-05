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
import com.meigsmart.test741.service.CpuService4;
import com.meigsmart.test741.service.CpuService5;
import com.meigsmart.test741.util.CpuMessage;
import com.meigsmart.test741.util.DateUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.text.NumberFormat;

public class CpuActivity extends BaseActivity {
    private TextView mTitle;
    private TextView mResult;

    private int mBroadType = 0;
    private TypeModel model;
    private Intent intent1;
    private Intent intent2;
    private Intent intent3;
    private Intent intent4;
    private Intent intent5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cpu);
        mTitle = (TextView) findViewById(R.id.title);
        mResult = (TextView) findViewById(R.id.result);
        mTitle.setText("CPU TEST");

        mBroadType = getIntent().getIntExtra("broadType", 0);

        if (mBroadType == 1) {
            String type = getIntent().getStringExtra("type");
            int time = getIntent().getIntExtra("time", 0);

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
    protected void error() {
        if (model != null) {
            MyApplication.getInstance().mDb.update(model.getType(), 0, 2);
        }
        this.finish();
    }

    @Override
    protected void success() {
        if (model != null) {
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
        mHandler.removeMessages(1001);
    }

    @Override
    public void onBackPressed() {
        exit();
        this.finish();
        super.onBackPressed();
    }

    private void init() {
        startService();
        calculateCpuUsage();
    }

    public void startService() {
        intent1 = new Intent(this, CpuService1.class);
        startService(intent1);

        intent2 = new Intent(this, CpuService2.class);
        startService(intent2);

        intent3 = new Intent(this, CpuService3.class);
        startService(intent3);

        intent4 = new Intent(this, CpuService4.class);
        startService(intent4);

        intent5 = new Intent(this, CpuService5.class);
        startService(intent5);
    }

    private void stopService() {
        stopService(intent1);
        stopService(intent2);
        stopService(intent3);
        stopService(intent4);
        stopService(intent5);
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
//                    NumberFormat nf = NumberFormat.getNumberInstance();
//                    nf.setMaximumFractionDigits(2);
//                    mResult.setText("CPU使用率："+nf.format(Double.parseDouble(msg.obj.toString()))+"%");
                    mResult.setText("CPU使用率：" + readUsage() + "%");
                    if (model != null) {
                        if (DateUtil.getTimeInterval(DateUtil.getCurrentDate(), model.getStartTime()) >= model.getAllTime() * 60) {
                            success();
                            stopService();
                            mHandler.removeMessages(1001);
                            CpuActivity.this.finish();
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
                runOnUiThread(new Runnable() {
                                  public void run() {
                                      mHandler.sendEmptyMessageDelayed(1001, 1000L);
                                  }
                              }
                );
            }
        }).start();
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
                int i = 0x1dcd6500;
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

}
