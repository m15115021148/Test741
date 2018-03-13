package com.meigsmart.test741.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.meigsmart.test741.MyApplication;
import com.meigsmart.test741.R;
import com.meigsmart.test741.config.RequestCode;
import com.meigsmart.test741.db.TypeModel;
import com.meigsmart.test741.util.CleanMessageUtil;
import com.meigsmart.test741.util.DateUtil;
import com.meigsmart.test741.util.FileUtil;
import com.meigsmart.test741.util.PreferencesUtil;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class MemoryActivity extends BaseActivity {
    private TextView mTitle;
    private TextView mResult;
    private TextView mOver;
    private TextView mExit;
    private ProgressBar mProgress;

    private int mBroadType = 0;

    private TypeModel model;

    private int progressValue;
    private int length;//标记文件大小
    private int type = 0;//0  assets中读取
    private String path = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memory);
        mTitle = findViewById(R.id.include).findViewById(R.id.title);
        mOver = findViewById(R.id.include).findViewById(R.id.over);
        mOver.setVisibility(View.VISIBLE);
        mExit = findViewById(R.id.include).findViewById(R.id.exit);
        mExit.setVisibility(View.VISIBLE);
        mResult = (TextView) findViewById(R.id.result);
        mProgress = (ProgressBar) findViewById(R.id.progressBar);
        mTitle.setText("MEMORY TEST");

        mBroadType = getIntent().getIntExtra("broadType", 0);

        if (mBroadType == 1) {
            path = FileUtil.createInnerPath("test.txt");
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

            init(model.getFilepath());

        } else if (mBroadType == 2) {
            mTitle.setText("EMMC TEST");
            path = FileUtil.createSDPath("test.txt");

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

            init(model.getFilepath());
        }

        mOver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MemoryActivity.this);
                builder.setTitle("提示");
                builder.setMessage("是否结束测试？");
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        success();
                        mHandler.removeMessages(1003);
                        mHandler.removeMessages(1004);
                        mHandler.removeMessages(1005);
                        mHandler.removeMessages(1006);
                        MemoryActivity.this.finish();
                    }
                });
                builder.setNegativeButton("取消",null);
                builder.create().show();
            }
        });

        mExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MemoryActivity.this);
                builder.setTitle("提示");
                builder.setMessage("是否退出整个测试，重新选择？");
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        MyApplication.getInstance().mDb.deleteAll();
                        PreferencesUtil.isFristLogin(MemoryActivity.this,"onClickStart",false);
                        PreferencesUtil.isFristLogin(MemoryActivity.this,"first",false);
                        PreferencesUtil.setStringData(MemoryActivity.this,"type","");

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

    private void init(final String path) {
        if (TextUtils.isEmpty(path)){
            type =0;
        }else{
            File file = new File(path);
            if (file.exists()) {
                type = 1;
                length = (int) file.length();
                mProgress.setMax(length);//设置进度条最大值
            }else{
                error();
                Toast.makeText(this,"路径不存在",Toast.LENGTH_SHORT).show();
                return;
            }
        }

        Thread thread = new Thread() {

            @Override
            public void run() {
                if (type == 1){
                    readFromFile(path);
                }else{
                    readFromResets();
                }
            }

        };
        thread.start();
    }

    @Override
    protected void error(){
        if (model != null){
            PreferencesUtil.setStringData(MemoryActivity.this,"type", RequestCode.ANDROID_ERROR);
            MyApplication.getInstance().mDb.update(model.getType(),0,2);
        }
        setResult(1001);
        this.finish();
    }

    @Override
    protected void success(){
        if (model != null){
            if (mBroadType == 1){
                PreferencesUtil.setStringData(MemoryActivity.this,"type", RequestCode.ANDROID_AUDIO);
            }else{
                PreferencesUtil.setStringData(MemoryActivity.this,"type", RequestCode.ANDROID_MEMORY);
            }
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
        mHandler.removeMessages(1003);
        mHandler.removeMessages(1004);
        mHandler.removeMessages(1005);
        mHandler.removeMessages(1006);
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

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1003:
                    progressValue += msg.arg1;
                    mProgress.setProgress(progressValue);
                    if(progressValue == length){
                        mHandler.sendEmptyMessage(1004);
                    }
                    mResult.setText(msg.obj.toString());
                    break;
                case 1004:
                    initWrite(mResult.getText().toString());
                    break;
                case 1005:
                    if (model != null) {
                        mResult.setText("");
                        progressValue = 0;
                        mProgress.setProgress(0);

                        init(path);

                        if (DateUtil.getTimeInterval(DateUtil.getCurrentDate(), model.getStartTime()) >= model.getAllTime() * 60) {
                            success();
                            mHandler.removeMessages(1003);
                            mHandler.removeMessages(1004);
                            mHandler.removeMessages(1005);
                            mHandler.removeMessages(1006);
                            MemoryActivity.this.finish();
                            break;
                        }
                    }
                    break;
                case 1006:
                    progressValue += msg.arg1;
                    mProgress.setProgress(progressValue);
                    if(progressValue == length){
                        mHandler.sendEmptyMessage(1005);
                    }
                    mResult.setText(msg.obj.toString());
                    break;
                default:
                    break;
            }
        }
    };

    private void initWrite(final String msg){
        if (TextUtils.isEmpty(msg)){
            error();
            return;
        }
        progressValue = 0;
        mProgress.setProgress(0);
        length = (int) msg.length();
        mProgress.setMax(length);//设置进度条最大值

        Thread thread = new Thread() {

            @Override
            public void run() {
                writeFile(msg);
            }

        };
        thread.start();
    }

    public void readFromFile(String path) {
        try {
            int line;
            FileInputStream fis = new FileInputStream(path);
            DataInputStream dis = new DataInputStream(fis);
            StringBuffer sb = new StringBuffer();
            byte b[] = new byte[1];
            while ((line = dis.read(b)) != -1) {
                String mData = new String(b, 0, line);
                sb.append(mData);
                Message msg = new Message();
                msg.what = 1003;
                msg.arg1 = line;
                msg.obj = sb.toString();
                mHandler.sendMessage(msg);
                Thread.sleep(50);
            }
            dis.close();
            fis.close();
        } catch (Exception e) {
            error();
            e.printStackTrace();
        }
    }

    private void writeFile(String data){
        try {
            OutputStream out = new FileOutputStream(path);
            InputStream is = new ByteArrayInputStream(data.getBytes());
            StringBuffer sb = new StringBuffer();
            byte[] buff = new byte[1];
            int len = 0;
            while((len=is.read(buff))!=-1){
                out.write(buff, 0, len);

                String mData = new String(buff, 0, len);
                sb.append(mData);

                Message msg = new Message();
                msg.what = 1006;
                msg.arg1 = len;
                msg.obj = data.replace(sb.toString(),"");
                mHandler.sendMessage(msg);
                Thread.sleep(50);
            }
            is.close();
            out.close();
        } catch (Exception e) {
            error();
            e.printStackTrace();
        }
    }

    public void readFromResets() {
        try {
            int line;
            InputStream is = this.getAssets().open("memory.txt");

            length = is.available();
            mProgress.setMax(length);//设置进度条最大值
            DataInputStream dis = new DataInputStream(is);
            StringBuffer sb = new StringBuffer();
            byte b[] = new byte[1];
            while ((line = dis.read(b)) != -1) {
                String mData = new String(b, 0, line);
                sb.append(mData);
                Message msg = new Message();
                msg.what = 1003;
                msg.arg1 = line;
                msg.obj = sb.toString();
                mHandler.sendMessage(msg);
                Thread.sleep(50);
            }
            dis.close();
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
            error();
        }

    }

}
