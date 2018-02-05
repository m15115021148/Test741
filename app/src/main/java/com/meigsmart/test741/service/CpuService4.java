package com.meigsmart.test741.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;


/**
 * Created by chenMeng on 2018/2/1.
 */

public class CpuService4 extends Service implements Runnable{
    private int sum = 1;
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 1001:
                    sum = sum*1000+1;
                    mHandler.postDelayed(CpuService4.this,1000);
                    break;
            }
        }
    };

    @Override
    public void run() {
        mHandler.sendEmptyMessage(1001);
    }

    public class MyBinder extends Binder {
        public CpuService4 getService() {
            return CpuService4.this;
        }
    }

    private MyBinder binder = new MyBinder();

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("result","r:"+CpuService4.class.getName());
        mHandler.post(this);
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(this);
        mHandler.removeMessages(1001);
    }

}
