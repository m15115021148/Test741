package com.meigsmart.test741;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.meigsmart.test741.activity.SplashActivity;
import com.meigsmart.test741.util.PreferencesUtil;

/**
 * Created by chenMeng on 2018/1/22.
 */

public class BootBroadcastReceiver extends BroadcastReceiver {
    static final String ACTION = "android.intent.action.BOOT_COMPLETED";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.e("result","action:"+action);

        if (action.equals(ACTION)) {
            if (PreferencesUtil.getFristLogin(context,"onClickStart")){
                Intent main = new Intent(context, SplashActivity.class);
                main.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(main);
            }
        }
    }
}
