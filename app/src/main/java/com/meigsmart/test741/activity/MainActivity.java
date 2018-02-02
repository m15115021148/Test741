package com.meigsmart.test741.activity;

import android.content.Context;
import android.content.IntentFilter;
import android.os.PowerManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;

import com.meigsmart.test741.BootBroadcastReceiver;
import com.meigsmart.test741.MainAdapter;
import com.meigsmart.test741.MyApplication;
import com.meigsmart.test741.R;
import com.meigsmart.test741.config.RequestCode;
import com.meigsmart.test741.db.TypeModel;
import com.meigsmart.test741.util.DateUtil;
import com.meigsmart.test741.util.PreferencesUtil;



public class MainActivity extends BaseActivity {
    private TextView mTitle;
    private ListView mLv;
    private BootBroadcastReceiver receiver;
    private String mType = "";
    private MainAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTitle = (TextView) findViewById(R.id.title);
        mLv = (ListView) findViewById(R.id.listView);

        receiver = new BootBroadcastReceiver();
        mTitle.setText("741测试");
        initListView();

        mType = PreferencesUtil.getStringData(this,"type");

        if (!TextUtils.isEmpty(mType)){
            initReboot();
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
    }

    private void initListView(){
        mAdapter = new MainAdapter(this);
        mLv.setAdapter(mAdapter);
    }

    private void initReboot() {
        TypeModel data = MyApplication.getInstance().mDb.getData(mType);

        if (data!=null && !TextUtils.isEmpty(data.getStartTime())){
            if (DateUtil.getTimeInterval(DateUtil.getCurrentDate(),data.getStartTime())<=data.getAllTime()*60){
                PowerManager pManager=(PowerManager) getSystemService(Context.POWER_SERVICE);
                pManager.reboot("重启");
            }else{
                MyApplication.getInstance().mDb.update(data.getType(),0,1);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    public void register(Context context) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(RequestCode.ANDROID_REBOOT);
        intentFilter.addAction(RequestCode.ANDROID_CPU);
        intentFilter.addAction(RequestCode.ANDROID_EMMC);
        intentFilter.addAction(RequestCode.ANDROID_MEMORY);
        intentFilter.addAction(RequestCode.ANDROID_AUDIO);
        intentFilter.addAction(RequestCode.ANDROID_VIDEO);
        intentFilter.addAction(RequestCode.ANDROID_LCD);
        context.registerReceiver(receiver, intentFilter);
    }

}
