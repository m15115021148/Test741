package com.meigsmart.test741.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;


/**
 * Created by chenMeng on 2018/2/1.
 */

public class CpuService1 extends Service implements Runnable{
    private int sum = 0;

    private Handler mHandler = new Handler();

    @Override
    public void run() {
        sum++;
        mHandler.postDelayed(this,1000);
    }

    public class MyBinder extends Binder {
        public CpuService1 getService() {
            return CpuService1.this;
        }
    }

    private CpuService1.MyBinder binder = new CpuService1.MyBinder();

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
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
    }

}
