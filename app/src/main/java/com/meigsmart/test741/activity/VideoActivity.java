package com.meigsmart.test741.activity;

import android.annotation.SuppressLint;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.meigsmart.test741.MyApplication;
import com.meigsmart.test741.R;
import com.meigsmart.test741.db.TypeModel;
import com.meigsmart.test741.util.DateUtil;

import java.io.File;

public class VideoActivity extends BaseActivity implements MediaPlayer.OnCompletionListener,MediaPlayer.OnErrorListener,MediaPlayer.OnInfoListener,
        MediaPlayer.OnPreparedListener, MediaPlayer.OnSeekCompleteListener,MediaPlayer.OnVideoSizeChangedListener,SurfaceHolder.Callback{
    private SurfaceView surfaceView;
    private SurfaceHolder holder;
    private MediaPlayer player;

    private int mBroadType = 0;

    private boolean isPlay;
    private TypeModel model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        surfaceView = (SurfaceView)findViewById(R.id.sf);

        mBroadType = getIntent().getIntExtra("broadType",0);

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

        if (mBroadType == 1){

            String type = getIntent().getStringExtra("type");
            int time = getIntent().getIntExtra("time",0);
            String path = getIntent().getStringExtra("filepath");

            MyApplication.getInstance().mDb.delete(type);

            model = new TypeModel();
            model.setType(type);
            model.setAllTime(time);
            model.setStartTime(DateUtil.getCurrentDate());
            model.setFilepath(path);
            model.setIsRun(1);
            model.setIsPass(0);

            MyApplication.getInstance().mDb.addData(model);

            isPlay = init(model.getFilepath());

        }
    }

    @Override
    protected void error(){
        if (model != null){
            MyApplication.getInstance().mDb.update(model.getType(),0,2);
        }
    }

    @Override
    protected void success(){
        if (model != null){
            MyApplication.getInstance().mDb.update(model.getType(),0,1);
        }
    }

    @Override
    protected void exit(){
        if (model != null){
            MyApplication.getInstance().mDb.update(model.getType(),0,0);
        }
    }

    private boolean init(String filepath){
        if (TextUtils.isEmpty(filepath)){
            error();
            Toast.makeText(this,"路径不能为空",Toast.LENGTH_SHORT).show();
            this.finish();
            return false;
        }else{
            File file = new File(filepath);
            if (file.exists()){
                try {
                    player.setDataSource(filepath);
                    return true;
                } catch (Exception e) {
                    error();
                    this.finish();
                    e.printStackTrace();
                }
            }else{
                error();
                Toast.makeText(this,"无法找到播放文件",Toast.LENGTH_SHORT).show();
                this.finish();
                return false;
            }
        }
        return false;
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
        if (isPlay){
            player.setLooping(true);
            player.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
            player.setDisplay(holder);
            player.prepareAsync();
        }
    }
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        player.release();
        player=null;
    }

    @Override
    public void onVideoSizeChanged(MediaPlayer arg0, int arg1, int arg2) {

    }

    @Override
    public void onSeekComplete(MediaPlayer arg0) {

    }

    @Override
    public void onPrepared(MediaPlayer player) {
        if (isPlay){
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.MATCH_PARENT);

            surfaceView.setLayoutParams(lp);
            player.start();
        }
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
        if (model!=null && !TextUtils.isEmpty(model.getStartTime())){
            int allTime = model.getAllTime();
            if (DateUtil.getTimeInterval(DateUtil.getCurrentDate(),model.getStartTime())<=allTime*60){
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