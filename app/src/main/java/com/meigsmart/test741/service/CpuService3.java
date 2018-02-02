package com.meigsmart.test741.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;


/**
 * Created by chenMeng on 2018/2/1.
 */

public class CpuService3 extends Service {

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 1001:
                    readFromResets();
                    break;
            }
        }
    };

    public class MyBinder extends Binder {
        public CpuService3 getService() {
            return CpuService3.this;
        }
    }

    private MyBinder binder = new MyBinder();

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        readFromResets();
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

    public void readFromResets() {
        try {
            InputStream is = this.getAssets().open("memory.txt");

            BufferedReader br = new BufferedReader(new InputStreamReader(is,"UTF-8"));
            StringBuffer sb = new StringBuffer();
            String line = br.readLine();
            while(line != null){
                sb.append(line).append("\n");
                line = br.readLine();
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mHandler.sendEmptyMessage(1001);
        }

    }

}
