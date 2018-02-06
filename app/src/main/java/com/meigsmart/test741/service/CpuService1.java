package com.meigsmart.test741.service;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;


/**
 * Created by chenMeng on 2018/2/1.
 */

public class CpuService1 extends Service implements Runnable{

    private void useCpu(final Activity activity) {
        (new Thread(new Runnable() {

            public void run() {
                int i = 100000000;
                do {
                    if (i == 1) {
                        activity.runOnUiThread(new Runnable() {
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

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1001:
                    mHandler.sendEmptyMessage(1002);
                    mHandler.postDelayed(CpuService1.this,1000);
                    break;
                case 1002:
//                    useCpu();
                    break;
            }

        }
    };

    @Override
    public void onCreate() {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mHandler.post(this);
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void run() {
        mHandler.sendEmptyMessage(1001);
    }
}
