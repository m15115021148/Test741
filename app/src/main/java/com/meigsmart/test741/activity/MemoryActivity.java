package com.meigsmart.test741.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.meigsmart.test741.MyApplication;
import com.meigsmart.test741.R;
import com.meigsmart.test741.db.TypeModel;
import com.meigsmart.test741.util.DateUtil;
import com.meigsmart.test741.util.FileUtil;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class MemoryActivity extends BaseActivity {
    private TextView mTitle;
    private TextView mResult;
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
        mTitle = (TextView) findViewById(R.id.title);
        mResult = (TextView) findViewById(R.id.result);
        mProgress = (ProgressBar) findViewById(R.id.progressBar);
        mTitle.setText("MEMORY TEST");

        mBroadType = getIntent().getIntExtra("broadType", 0);

        if (mBroadType == 1) {
            path = FileUtil.createInnerPath("test.txt");
            String type = getIntent().getStringExtra("type");
            int time = getIntent().getIntExtra("time", 0);
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

            init(model.getFilepath());

        } else if (mBroadType == 2) {
            mTitle.setText("EMMC TEST");
            path = FileUtil.createSDPath("test.txt");

            String type = getIntent().getStringExtra("type");
            int time = getIntent().getIntExtra("time", 0);
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

            init(model.getFilepath());
        }
    }

    private void init(final String path) {
        if (TextUtils.isEmpty(path)) return;
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
            MyApplication.getInstance().mDb.update(model.getType(),0,2);
        }
        this.finish();
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
            InputStream is = this.getAssets().open("memory.txt");

            length = is.available();
            mProgress.setMax(length);//设置进度条最大值

            BufferedReader br = new BufferedReader(new InputStreamReader(is,"UTF-8"));
            StringBuffer sb = new StringBuffer();
            String line = br.readLine();
            while(line != null){
                sb.append(line).append("\n");
                line=br.readLine();
                Message msg = new Message();
                msg.what = 1003;
                msg.arg1 = line.length();
                msg.obj = sb.toString();
                mHandler.sendMessage(msg);
                Thread.sleep(10);
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
