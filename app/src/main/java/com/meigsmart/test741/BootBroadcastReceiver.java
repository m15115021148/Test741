package com.meigsmart.test741;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.meigsmart.test741.activity.AudioActivity;
import com.meigsmart.test741.activity.CpuActivity;
import com.meigsmart.test741.activity.MainActivity;
import com.meigsmart.test741.activity.MemoryActivity;
import com.meigsmart.test741.activity.TestActivity;
import com.meigsmart.test741.activity.VideoActivity;
import com.meigsmart.test741.config.RequestCode;
import com.meigsmart.test741.db.TypeModel;
import com.meigsmart.test741.util.DateUtil;

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
            Intent main = new Intent(context, MainActivity.class);
            main.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(main);
        } else if (action.equals(RequestCode.ANDROID_REBOOT)){
            if(!check(action))return;

            Intent test = new Intent(context, TestActivity.class);
            test.putExtra("broadType",2);
            test.putExtra("type", action);
            test.putExtra("time", intent.getIntExtra("time",0));
            context.startActivity(test);
        } else if (action.equals(RequestCode.ANDROID_LCD)){
            if(!check(action))return;

            Intent test = new Intent(context, TestActivity.class);
            test.putExtra("broadType",1);
            test.putExtra("type", action);
            test.putExtra("time", intent.getIntExtra("time",0));
            context.startActivity(test);
        } else if (action.equals(RequestCode.ANDROID_VIDEO)){
            if(!check(action))return;

            Intent video = new Intent(context, VideoActivity.class);
            video.putExtra("broadType",1);
            video.putExtra("type", action);
            video.putExtra("filepath",intent.getStringExtra("filepath"));
            video.putExtra("time", intent.getIntExtra("time",0));
            context.startActivity(video);
        } else if (action.equals(RequestCode.ANDROID_MEMORY)){
            if(!check(action))return;

            Intent video = new Intent(context, MemoryActivity.class);
            video.putExtra("broadType",1);
            video.putExtra("type", action);
            video.putExtra("filepath",intent.getStringExtra("filepath"));
            video.putExtra("time", intent.getIntExtra("time",0));
            context.startActivity(video);
        } else if (action.equals(RequestCode.ANDROID_EMMC)){
            if(!check(action))return;

            Intent emmc = new Intent(context, MemoryActivity.class);
            emmc.putExtra("broadType",2);
            emmc.putExtra("type", action);
            emmc.putExtra("filepath",intent.getStringExtra("filepath"));
            emmc.putExtra("time", intent.getIntExtra("time",0));
            context.startActivity(emmc);
        } else if (action.equals(RequestCode.ANDROID_CPU)){
            if(!check(action))return;

            Intent cpu = new Intent(context, CpuActivity.class);
            cpu.putExtra("broadType",1);
            cpu.putExtra("type", action);
            cpu.putExtra("time", intent.getIntExtra("time",0));
            context.startActivity(cpu);
        } else if (action.equals(RequestCode.ANDROID_AUDIO)){
            if(!check(action))return;

            Intent audio = new Intent(context, AudioActivity.class);
            audio.putExtra("broadType",1);
            audio.putExtra("type", action);
            audio.putExtra("time", intent.getIntExtra("time",0));
            audio.putExtra("filepath",intent.getStringExtra("filepath"));
            context.startActivity(audio);
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
}
