package com.meigsmart.test741.activity;

import android.animation.ObjectAnimator;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.KeyEvent;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.meigsmart.test741.MusicService;
import com.meigsmart.test741.MyApplication;
import com.meigsmart.test741.R;
import com.meigsmart.test741.config.RequestCode;
import com.meigsmart.test741.db.TypeModel;
import com.meigsmart.test741.util.DateUtil;
import com.meigsmart.test741.util.PreferencesUtil;

import java.text.SimpleDateFormat;

public class AudioActivity extends BaseActivity implements Runnable{
    private TextView mTitle;
    private TextView mCurrTime,mTotalTime;
    private SeekBar mSb;
    private ImageView mImg;

    private int mBroadType = 0;

    private SimpleDateFormat time = new SimpleDateFormat("mm:ss");
    private MusicService musicService;
    public Handler handler = new Handler();
    private ObjectAnimator animator;
    private TypeModel model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio);
        mTitle = (TextView)findViewById(R.id.title);
        mCurrTime = (TextView)findViewById(R.id.MusicTime);
        mTotalTime = (TextView)findViewById(R.id.MusicTotal);
        mSb = (SeekBar)findViewById(R.id.MusicSeekBar);
        mImg = (ImageView)findViewById(R.id.image);
        mTitle.setText("AUDIO TEST");

        mBroadType = getIntent().getIntExtra("broadType",0);

        if (mBroadType == 1){
            String type = getIntent().getStringExtra("type");
            int time = Integer.parseInt(PreferencesUtil.getStringData(this,"time"));
            String path = "";

            MyApplication.getInstance().mDb.delete(type);

            model = new TypeModel();
            model.setType(type);
            model.setAllTime(time);
            model.setStartTime(DateUtil.getCurrentDate());
            model.setFilepath(path);
            model.setIsRun(1);
            model.setIsPass(0);

            MyApplication.getInstance().mDb.addData(model);

            bindServiceConnection();
        }
    }

    @Override
    protected void error(){
        if (model != null){
            PreferencesUtil.setStringData(this,"type", RequestCode.ANDROID_ERROR);
            MyApplication.getInstance().mDb.update(model.getType(),0,2);
        }
        this.finish();
    }

    @Override
    protected void success(){
        if (model != null){
            PreferencesUtil.setStringData(this,"type", RequestCode.ANDROID_VIDEO);
            MyApplication.getInstance().mDb.update(model.getType(),0,1);
        }
    }

    @Override
    protected void exit(){
        if (model != null){
            MyApplication.getInstance().mDb.update(model.getType(),0,0);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(this);
    }

    @Override
    public void onBackPressed() {
        exit();
        this.finish();
        super.onBackPressed();
    }

    private void bindServiceConnection() {
        Intent intent = new Intent(this, MusicService.class);
        startService(intent);
        bindService(intent, serviceConnection, this.BIND_AUTO_CREATE);
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            musicService = ((MusicService.MyBinder) (service)).getService(model);
            handler.post(AudioActivity.this);
            mTotalTime.setText(time.format(musicService.mediaPlayer.getDuration()));
            rotationImg();
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicService = null;
        }
    };


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(false);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void run() {
        if (musicService.isPlay){
            mCurrTime.setText(time.format(musicService.mediaPlayer.getCurrentPosition()));
            mSb.setProgress(musicService.mediaPlayer.getCurrentPosition());
            mSb.setMax(musicService.mediaPlayer.getDuration());
            mTotalTime.setText(time.format(musicService.mediaPlayer.getDuration()));
            handler.postDelayed(this, 200);
        }else {
            handler.removeCallbacks(this);
            unbindService(serviceConnection);
            Intent intent = new Intent(this, MusicService.class);
            stopService(intent);
            this.finish();
        }
    }

    private void rotationImg(){
        animator = ObjectAnimator.ofFloat(mImg, "rotation", 0f, 360.0f);
        animator.setDuration(10000);
        animator.setInterpolator(new LinearInterpolator());
        animator.setRepeatCount(-1);
        animator.start();
    }
}
