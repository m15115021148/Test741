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

public class CpuService5 extends Service {

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
        public CpuService5 getService() {
            return CpuService5.this;
        }
    }

    private MyBinder binder = new MyBinder();

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("result","r:"+CpuService5.class.getName());
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
        mHandler.removeMessages(1001);
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
