package com.meigsmart.test741;

import android.app.Service;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.text.TextUtils;

import com.meigsmart.test741.config.RequestCode;
import com.meigsmart.test741.db.TypeModel;
import com.meigsmart.test741.util.DateUtil;
import com.meigsmart.test741.util.PreferencesUtil;

import java.io.File;
import java.io.IOException;

/**
 * Created by chenMeng on 2018/1/30.
 */

public class MusicService extends Service {
    public MediaPlayer mediaPlayer;
    public boolean isPlay = false;

    public MusicService() {
    }

    private MusicService getInstance(final TypeModel model){
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.reset();
        try {
            if (TextUtils.isEmpty(model.getFilepath())){
                setDefaultDataSource();
            }else{
                File file = new File(model.getFilepath());
                if (file.exists()){
                    try {
                        mediaPlayer.setDataSource(this, Uri.parse(model.getFilepath()));
                        mediaPlayer.prepare();
                    } catch (Exception e) {
                        MyApplication.getInstance().mDb.update(model.getType(),0,2);
                        e.printStackTrace();
                    }
                }else{
                    setDefaultDataSource();
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            MyApplication.getInstance().mDb.update(model.getType(),0,2);
            setDefaultDataSource();
        }
        mediaPlayer.setLooping(false);
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                isPlay = true;
                mp.start();
            }
        });
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (!TextUtils.isEmpty(model.getStartTime())){
                    if (DateUtil.getTimeInterval(DateUtil.getCurrentDate(),model.getStartTime())<=model.getAllTime()*60){
                        isPlay = true;
                        mp.seekTo(0);
                        mp.start();
                    }else{
                        PreferencesUtil.setStringData(getApplicationContext(),"type", RequestCode.ANDROID_VIDEO);
                        MyApplication.getInstance().mDb.update(model.getType(),0,1);
                        isPlay = false;
                        mp.stop();
                        mp.release();
                    }
                }
            }
        });
        return MusicService.this;
    }

    private void setDefaultDataSource(){
        try {
            AssetFileDescriptor afd = this.getAssets().openFd("music.mp3");
            mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            mediaPlayer.prepare();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    public MyBinder binder = new MyBinder();

    public class MyBinder extends Binder {
        public MusicService getService(TypeModel m) {
            return getInstance(m);
        }
    }

    public void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }
}
