package com.meigsmart.test741.service;

import android.annotation.SuppressLint;
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

public class CpuService1 extends Service {
    private Looper mServiceLooper;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Intent intent = (Intent)msg.obj;

        }
    };

    @Override
    public void onCreate() {
        HandlerThread handlerthread = new HandlerThread("CpuService1");
        handlerthread.start();
        mServiceLooper = handlerthread.getLooper();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Message message = mHandler.obtainMessage();
        mHandler.sendMessage(message);
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

}
