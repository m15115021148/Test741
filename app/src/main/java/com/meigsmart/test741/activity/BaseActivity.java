package com.meigsmart.test741.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

public abstract class BaseActivity extends Activity {
    protected static final String TAG_ESC_ACTIVITY = "com.broader.esc";//内容描述 退出activity时 发送的广播信号
    private MyBroaderEsc receiver;//广播

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 设置无标题
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        // 防止锁屏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // 注册广播
        receiver = new MyBroaderEsc();
        registerReceiver(receiver, new IntentFilter(TAG_ESC_ACTIVITY));

    }

    protected abstract void success();
    protected abstract void error();
    protected abstract void exit();

    /**
     * @发送广播 退出activity
     *
     */
    class MyBroaderEsc extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                finish();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }


}
