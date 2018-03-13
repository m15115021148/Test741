package com.meigsmart.test741.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.meigsmart.test741.MyApplication;
import com.meigsmart.test741.R;
import com.meigsmart.test741.config.RequestCode;
import com.meigsmart.test741.db.TypeModel;
import com.meigsmart.test741.util.CleanMessageUtil;
import com.meigsmart.test741.util.DateUtil;
import com.meigsmart.test741.util.PreferencesUtil;

import java.io.IOException;

public class VideoActivity extends BaseActivity implements MediaPlayer.OnCompletionListener,MediaPlayer.OnErrorListener,MediaPlayer.OnInfoListener,
        MediaPlayer.OnPreparedListener, MediaPlayer.OnSeekCompleteListener,MediaPlayer.OnVideoSizeChangedListener,SurfaceHolder.Callback{
    private SurfaceView surfaceView;
    private SurfaceHolder holder;
    private MediaPlayer player;

    private int mBroadType = 0;

    private TypeModel model;
    private TextView mOver;
    private TextView mExit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        surfaceView = (SurfaceView)findViewById(R.id.sf);
        mOver = findViewById(R.id.over);
        mExit = findViewById(R.id.exit);

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
        }

        mOver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(VideoActivity.this);
                builder.setTitle("提示");
                builder.setMessage("是否结束测试？");
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        success();
                        if (player!=null){
                            player.stop();
                            player.release();
                        }
                        VideoActivity.this.finish();
                    }
                });
                builder.setNegativeButton("取消",null);
                builder.create().show();
            }
        });

        mExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(VideoActivity.this);
                builder.setTitle("提示");
                builder.setMessage("是否退出整个测试，重新选择？");
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        MyApplication.getInstance().mDb.deleteAll();
                        PreferencesUtil.isFristLogin(VideoActivity.this,"onClickStart",false);
                        PreferencesUtil.isFristLogin(VideoActivity.this,"first",false);
                        PreferencesUtil.setStringData(VideoActivity.this,"type","");

                        CleanMessageUtil.cleanApplicationData(MyApplication.getInstance().getApplicationContext());

                        //退出所有的activity
                        Intent intent = new Intent();
                        intent.setAction(BaseActivity.TAG_ESC_ACTIVITY);
                        sendBroadcast(intent);
                        finish();
                    }
                });
                builder.setNegativeButton("取消",null);
                builder.create().show();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        holder = surfaceView.getHolder();
        holder.addCallback(this);
        //为了可以播放视频或者使用Camera预览，我们需要指定其Buffer类型
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        //下面开始实例化MediaPlayer对象
        player = new MediaPlayer();
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
        player.setOnInfoListener(this);
        player.setOnPreparedListener(this);
        player.setOnSeekCompleteListener(this);
        player.setOnVideoSizeChangedListener(this);
    }

    @Override
    protected void error(){
        if (model != null){
            PreferencesUtil.setStringData(this,"type", RequestCode.ANDROID_ERROR);
            MyApplication.getInstance().mDb.update(model.getType(),0,2);
            setResult(1001);
        }
    }

    @Override
    protected void success(){
        if (model != null){
            PreferencesUtil.setStringData(this,"type", RequestCode.ANDROID_LCD);
            MyApplication.getInstance().mDb.update(model.getType(),0,1);
            setResult(1001);
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
        if (player!=null){
            player.stop();
            player.release();
            player=null;
        }
    }

    @Override
    public void onBackPressed() {
        exit();
        this.finish();
        super.onBackPressed();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(false);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {

    }

    @SuppressLint("NewApi")
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if ( player != null ){
            try {
                player.reset();
                player.setDisplay(holder);
                AssetManager assetMg = this.getApplicationContext().getAssets();
                AssetFileDescriptor fileDescriptor = assetMg.openFd("test1.mp4");
                player.setDataSource(fileDescriptor.getFileDescriptor(), fileDescriptor.getStartOffset(), fileDescriptor.getLength());
                player.prepareAsync();
            } catch (IOException e) {
                e.printStackTrace();
                error();
                Toast.makeText(this,"无法找到播放文件",Toast.LENGTH_SHORT).show();
                this.finish();
            }
        }

    }
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {//后台
        if (player!=null){
            //释放资源
            player.release();
            player = null;
        }

    }

    @Override
    public void onVideoSizeChanged(MediaPlayer arg0, int arg1, int arg2) {

    }

    @Override
    public void onSeekComplete(MediaPlayer arg0) {

    }

    @Override
    public void onPrepared(MediaPlayer player) {
        Log.e("result","onPrepared...");
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);

        surfaceView.setLayoutParams(lp);
        player.start();
    }
    @Override
    public boolean onInfo(MediaPlayer player, int whatInfo, int extra) {
        // 当一些特定信息出现或者警告时触发
        switch(whatInfo){
            case MediaPlayer.MEDIA_INFO_BAD_INTERLEAVING:
                break;
            case MediaPlayer.MEDIA_INFO_METADATA_UPDATE:
                break;
            case MediaPlayer.MEDIA_INFO_VIDEO_TRACK_LAGGING:
                break;
            case MediaPlayer.MEDIA_INFO_NOT_SEEKABLE:
                break;
        }
        return false;
    }

    @Override
    public boolean onError(MediaPlayer player, int whatError, int extra) {
        error();
        switch (whatError) {
            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                break;
            case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                break;
            default:
                break;
        }
        return false;
    }

    @Override
    public void onCompletion(MediaPlayer player) {
        TypeModel data = MyApplication.getInstance().mDb.getData(RequestCode.ANDROID_VIDEO);
        if (data!=null && !TextUtils.isEmpty(data.getStartTime())){
            int allTime = data.getAllTime();
            if (DateUtil.getTimeInterval(DateUtil.getCurrentDate(),data.getStartTime())<=allTime*60){
                player.seekTo(0);
                player.start();
            }else{
                success();
                player.stop();
                player.release();
                this.finish();
            }
        }
    }

}
