package com.meigsmart.test741.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ListView;

import com.meigsmart.test741.BootBroadcastReceiver;
import com.meigsmart.test741.MainAdapter;
import com.meigsmart.test741.MyApplication;
import com.meigsmart.test741.R;
import com.meigsmart.test741.config.RequestCode;
import com.meigsmart.test741.db.TypeModel;
import com.meigsmart.test741.util.DateUtil;
import com.meigsmart.test741.util.PreferencesUtil;

public class MainActivity extends BaseActivity {
    private ListView mLv;
    private String mType = "";
    private MainAdapter mAdapter;
    private BootBroadcastReceiver receiver = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mLv = (ListView) findViewById(R.id.listView);
        receiver = new BootBroadcastReceiver();
        initListView();

        int time = 60/7;
        //time = 1;
        PreferencesUtil.setStringData(this,"time",String.valueOf(time));

    }

    @Override
    protected void onResume() {
        super.onResume();
        register(this);
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (mAdapter!=null)mAdapter.setData(MyApplication.getInstance().mDb.getAllData());

        mHandler.sendEmptyMessageDelayed(1001,3000);

    }

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 1001:
                    if (PreferencesUtil.getFristLogin(MainActivity.this,"first") && TextUtils.isEmpty(mType)){
                        mType = RequestCode.ANDROID_REBOOT;
                    }else{
                        mType = PreferencesUtil.getStringData(MainActivity.this,"type");
                    }

                    init(mType);
                    break;
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (receiver!=null)unregisterReceiver(receiver);
    }

    private void init(String type){
        if (RequestCode.ANDROID_REBOOT.equals(type)){
            initReboot(type);
        }else if (RequestCode.ANDROID_CPU.equals(type)){
            if(!check(type))return;

            Intent cpu = new Intent(this, CpuActivity.class);
            cpu.putExtra("broadType",1);
            cpu.putExtra("type", type);
            startActivity(cpu);
        }else if (RequestCode.ANDROID_EMMC.equals(type)){
            if(!check(type))return;

            Intent emmc = new Intent(this, MemoryActivity.class);
            emmc.putExtra("broadType",2);
            emmc.putExtra("type", type);
            startActivity(emmc);
        }else if (RequestCode.ANDROID_MEMORY.equals(type)){
            if(!check(type))return;

            Intent video = new Intent(this, MemoryActivity.class);
            video.putExtra("broadType",1);
            video.putExtra("type", type);
            startActivity(video);
        }else if (RequestCode.ANDROID_AUDIO.equals(type)){
            if(!check(type))return;

            Intent audio = new Intent(this, AudioActivity.class);
            audio.putExtra("broadType",1);
            audio.putExtra("type", type);
            startActivity(audio);
        }else if (RequestCode.ANDROID_VIDEO.equals(type)){
            if(!check(type))return;

            Intent video = new Intent(this, VideoActivity.class);
            video.putExtra("broadType",1);
            video.putExtra("type", type);
            startActivity(video);
        }else if (RequestCode.ANDROID_LCD.equals(type)){
            if(!check(type))return;

            Intent test = new Intent(this, TestActivity.class);
            test.putExtra("broadType",1);
            test.putExtra("type", type);
            startActivity(test);
        } else if (RequestCode.ANDROID_ERROR.equals(type)){
            PreferencesUtil.isFristLogin(this,"first",false);
            PreferencesUtil.setStringData(this,"type","");
        } else if (RequestCode.ANDROID_SUCCESS.equals(type)){
            PreferencesUtil.setStringData(this,"allTime",String.valueOf(Integer.parseInt(PreferencesUtil.getStringData(this,"allTime"))-1));
            if (Integer.parseInt(PreferencesUtil.getStringData(this,"allTime"))==0){
                PreferencesUtil.isFristLogin(this,"first",false);
                PreferencesUtil.setStringData(this,"type","");
            }else {
                PreferencesUtil.setStringData(this,"type", RequestCode.ANDROID_REBOOT);
                mHandler.sendEmptyMessageDelayed(1001,3000);
            }
        }
    }

    @Override
    protected void success() {
    }

    @Override
    protected void error() {
    }

    @Override
    protected void exit() {
    }

    private void initListView(){
        mAdapter = new MainAdapter(this);
        mLv.setAdapter(mAdapter);
    }

    private void initReboot(String type) {
        if (!PreferencesUtil.getFristLogin(this,"reboot")){

            int time = Integer.parseInt(PreferencesUtil.getStringData(this,"time"));
            PreferencesUtil.setStringData(this,"type",type);

            MyApplication.getInstance().mDb.delete(type);

            TypeModel model = new TypeModel();
            model.setType(type);
            model.setAllTime(time);
            model.setStartTime(DateUtil.getCurrentDate());
            model.setFilepath("");
            model.setIsRun(1);
            model.setIsPass(0);

            MyApplication.getInstance().mDb.addData(model);
            PreferencesUtil.isFristLogin(this,"reboot",true);

            // 重启
            PowerManager pManager=(PowerManager) getSystemService(Context.POWER_SERVICE);
            pManager.reboot("重启");
        }else{
            TypeModel data = MyApplication.getInstance().mDb.getData(mType);

            if (data!=null && !TextUtils.isEmpty(data.getStartTime())){
                if (DateUtil.getTimeInterval(DateUtil.getCurrentDate(),data.getStartTime())<=data.getAllTime()*60){
                    PowerManager pManager=(PowerManager) getSystemService(Context.POWER_SERVICE);
                    pManager.reboot("重启");
                }else{
                    PreferencesUtil.isFristLogin(this,"reboot",false);
                    MyApplication.getInstance().mDb.update(data.getType(),0,1);

                    PreferencesUtil.setStringData(this,"type",RequestCode.ANDROID_CPU);
                    onResume();
                }
            }
        }
    }

    private boolean check(String action){
        boolean isRun = MyApplication.getInstance().mDb.checkIsRun();
        if (isRun){
            return false;
        }

        TypeModel data = MyApplication.getInstance().mDb.getData(action);

        if (data!=null && !TextUtils.isEmpty(data.getStartTime())){
            int time = data.getAllTime();
            if (DateUtil.getTimeInterval(DateUtil.getCurrentDate(),data.getStartTime())<=time*60){
                return false;
            }
        }
        Log.w("result","isRun:"+isRun);
        return true;
    }

    public void register(Context context) {
        IntentFilter intentFilter = new IntentFilter();
//        intentFilter.addAction(RequestCode.ANDROID_REBOOT);
//        intentFilter.addAction(RequestCode.ANDROID_CPU);
//        intentFilter.addAction(RequestCode.ANDROID_EMMC);
//        intentFilter.addAction(RequestCode.ANDROID_MEMORY);
//        intentFilter.addAction(RequestCode.ANDROID_AUDIO);
//        intentFilter.addAction(RequestCode.ANDROID_VIDEO);
//        intentFilter.addAction(RequestCode.ANDROID_LCD);
        context.registerReceiver(receiver, intentFilter);
    }
}
