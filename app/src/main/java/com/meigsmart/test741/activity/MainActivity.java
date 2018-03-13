package com.meigsmart.test741.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.meigsmart.test741.BootBroadcastReceiver;
import com.meigsmart.test741.MainAdapter;
import com.meigsmart.test741.MyApplication;
import com.meigsmart.test741.R;
import com.meigsmart.test741.config.RequestCode;
import com.meigsmart.test741.db.TypeModel;
import com.meigsmart.test741.util.CleanMessageUtil;
import com.meigsmart.test741.util.DateUtil;
import com.meigsmart.test741.util.DeviceUtil;
import com.meigsmart.test741.util.FileUtil;
import com.meigsmart.test741.util.PreferencesUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MainActivity extends BaseActivity implements MainAdapter.OnMainCallBack{
    private Context mContext;
    private ListView mLv;
    private String mType = "";
    private MainAdapter mAdapter;
    private BootBroadcastReceiver receiver = null;
    private TextView mTestResult;
    private StringBuffer resultStringBuffer = new StringBuffer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        mLv =  findViewById(R.id.listView);
        mTestResult = findViewById(R.id.include).findViewById(R.id.over);
        receiver = new BootBroadcastReceiver();
        register(this);
        initListView();

        int time = 60/7;
//        time = 1;
        PreferencesUtil.setStringData(this,"time",String.valueOf(time));
        mType = PreferencesUtil.getStringData(MainActivity.this,"type");

        initData();
    }

    private void initData() {
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (mAdapter!=null)mAdapter.setData(MyApplication.getInstance().mDb.getAllData());

        mHandler.sendEmptyMessageDelayed(1001,3000);
    }

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 1001:
                    if (PreferencesUtil.getFristLogin(MainActivity.this,"first") && TextUtils.isEmpty(mType)){
                        mType = RequestCode.ANDROID_EMMC;
                    }else{
                        mType = PreferencesUtil.getStringData(MainActivity.this,"type");
                    }

                    init(mType);
                    break;
                case 1002:
                    if (mAdapter!=null)mAdapter.setData(MyApplication.getInstance().mDb.getAllData());
                    mHandler.sendEmptyMessageDelayed(1001,3000);
                    break;
                case 1003:
                    PreferencesUtil.isFristLogin(MainActivity.this,"reboot",false);
                    MyApplication.getInstance().mDb.update(RequestCode.ANDROID_REBOOT,0,1);

                    PreferencesUtil.setStringData(MainActivity.this,"type",RequestCode.ANDROID_SUCCESS);
                    mHandler.sendEmptyMessageDelayed(1002,5000);
                    break;
                case 1004://error
                    if (mAdapter!=null)mAdapter.setData(MyApplication.getInstance().mDb.getAllData());
                    saveData();

                    mHandler.sendEmptyMessageDelayed(1006,2000);
                    break;
                case 1005://success
                    mTestResult.setVisibility(View.VISIBLE);
                    mTestResult.setText("测试完成");
                    if (mAdapter!=null)mAdapter.setData(MyApplication.getInstance().mDb.getAllData());
                    saveData();

                    mHandler.sendEmptyMessageDelayed(1006,2000);
                    break;
                case 1006:
                    MyApplication.getInstance().mDb.deleteAll();
                    Intent intent = new Intent(mContext,SplashActivity.class);
                    startActivity(intent);
                    finish();
                    break;
            }
        }
    };

    private void saveData(){
        resultStringBuffer.setLength(0);
        //save data
        String data = FileUtil.readFile(FileUtil.getSDPath(mContext, true), RequestCode.GET_CONFIG_PATH);
        String productionCode = "";
        String staffCode = "";
        if (!TextUtils.isEmpty(data)){
            try {
                JSONObject object = new JSONObject(data);
                productionCode = object.getString("RUNIN_ProductionCode");
                staffCode = object.getString("RUNIN_StaffCode");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }


        String result = getTestResultJson(MyApplication.getInstance().mDb.getAllData());

        String objectJson = getObjectJson(result, getAllResult(MyApplication.getInstance().mDb.getAllData()), productionCode, staffCode);

        FileUtil.writeFile(FileUtil.createFolder(FileUtil.getStoragePath()),RequestCode.SAVE_RESULT_PATH,objectJson);
    }

    private String getObjectJson(String result,boolean type,String ws,String empCode){
        append(RequestCode.JSON_SN,Build.SERIAL);
        append(RequestCode.JSON_MACADDR,DeviceUtil.getMac());
        append(RequestCode.JSON_BTMACADDR,DeviceUtil.getBluetoothAddress());
        StringBuffer buffer = new StringBuffer();
        buffer.append("000000000000000");
        buffer.append(TextUtils.isEmpty(DeviceUtil.getMac())?"":DeviceUtil.getMac().replace(":","").toUpperCase());
        append(RequestCode.JSON_UUID,buffer.toString());
       // append(RequestCode.JSON_SKU,"");
        append(RequestCode.JSON_TR,result);
        append(RequestCode.JSON_RST,type?"Success":"Failure");
        append(RequestCode.JSON_WS,ws);
        append1(RequestCode.JSON_EMPCODE,empCode);
    //    append(RequestCode.JSON_FULLYCHARGED,"");
     //   append1(RequestCode.JSON_ISCHARGED,"");
        return resultStringBuffer.toString();
    }

    private String getTestResultJson(List<TypeModel> list){
        JSONObject object = new JSONObject();
        String r = "";
        try {
            if (list.size()>0){
                for (int i=0;i<list.size();i++){
                    TypeModel model = list.get(i);
                    if (model.getIsPass() == 0){
                        r = "NO TEST";
                    }else if (model.getIsPass() == 1){
                        r = "Success";
                    }else {
                        r = "Failure";
                    }
                    if (RequestCode.ANDROID_EMMC.equals(model.getType())){
                        object.put(RequestCode.RESULT_EMMC,r);
                    } else if (RequestCode.ANDROID_MEMORY.equals(model.getType())){
                        object.put(RequestCode.RESULT_MEMORY,r);
                    } else if (RequestCode.ANDROID_AUDIO.equals(model.getType())){
                        object.put(RequestCode.RESULT_AUDIO,r);
                    } else if (RequestCode.ANDROID_VIDEO.equals(model.getType())){
                        object.put(RequestCode.RESULT_VIDEO,r);
                    } else if (RequestCode.ANDROID_LCD.equals(model.getType())){
                        object.put(RequestCode.RESULT_LCD,r);
                    } else if (RequestCode.ANDROID_CPU.equals(model.getType())){
                        object.put(RequestCode.RESULT_CPU,r);
                    } else if (RequestCode.ANDROID_REBOOT.equals(model.getType())){
                        object.put(RequestCode.RESULT_REBOOT,r);
                    }
                }
            }
//            else{
//                r = "NO TEST";
//                object.put(RequestCode.RESULT_EMMC,r);
//                object.put(RequestCode.RESULT_MEMORY,r);
//                object.put(RequestCode.RESULT_AUDIO,r);
//                object.put(RequestCode.RESULT_VIDEO,r);
//                object.put(RequestCode.RESULT_LCD,r);
//                object.put(RequestCode.RESULT_CPU,r);
//                object.put(RequestCode.RESULT_REBOOT,r);
//            }
            return object.toString();
        }catch (JSONException e) {
            e.printStackTrace();
        }
        return "";
    }

    private boolean getAllResult(List<TypeModel> list){
        if (list.size()<=0){
            return false;
        }
        for (TypeModel model : list){
            if (model.getIsPass() == 2 || model.getIsPass() == 0){
                return false;
            }
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (receiver!=null)unregisterReceiver(receiver);
        mHandler.removeMessages(1001);
        mHandler.removeMessages(1002);
        mHandler.removeMessages(1003);
        mHandler.removeMessages(1004);
        mHandler.removeMessages(1005);
        mHandler.removeMessages(1006);
    }

    private void init(String type){
        if (RequestCode.ANDROID_REBOOT.equals(type)){
            initReboot(type);
        }else if (RequestCode.ANDROID_CPU.equals(type)){
            if(!check(type)){
                MyApplication.getInstance().mDb.update(type,0,2);
                PreferencesUtil.setStringData(mContext,"type", RequestCode.ANDROID_REBOOT);
                mHandler.sendEmptyMessage(1001);
                return;
            }

            Intent cpu = new Intent(this, CpuActivity.class);
            cpu.putExtra("broadType",1);
            cpu.putExtra("type", type);
            startActivityForResult(cpu,1001);
        }else if (RequestCode.ANDROID_EMMC.equals(type)){
            if(!check(type)){
                MyApplication.getInstance().mDb.update(type,0,2);
                PreferencesUtil.setStringData(mContext,"type", RequestCode.ANDROID_MEMORY);
                mHandler.sendEmptyMessage(1001);
                return;
            }

            Intent emmc = new Intent(this, MemoryActivity.class);
            emmc.putExtra("broadType",2);
            emmc.putExtra("type", type);
            startActivityForResult(emmc,1001);
        }else if (RequestCode.ANDROID_MEMORY.equals(type)){
            if(!check(type)){
                MyApplication.getInstance().mDb.update(type,0,2);
                PreferencesUtil.setStringData(mContext,"type", RequestCode.ANDROID_AUDIO);
                mHandler.sendEmptyMessage(1001);
                return;
            }

            Intent video = new Intent(this, MemoryActivity.class);
            video.putExtra("broadType",1);
            video.putExtra("type", type);
            startActivityForResult(video,1001);
        }else if (RequestCode.ANDROID_AUDIO.equals(type)){
            if(!check(type)){
                MyApplication.getInstance().mDb.update(type,0,2);
                PreferencesUtil.setStringData(mContext,"type", RequestCode.ANDROID_VIDEO);
                mHandler.sendEmptyMessage(1001);
                return;
            }

            Intent audio = new Intent(this, AudioActivity.class);
            audio.putExtra("broadType",1);
            audio.putExtra("type", type);
            startActivityForResult(audio,1001);
        }else if (RequestCode.ANDROID_VIDEO.equals(type)){
            if(!check(type)){
                MyApplication.getInstance().mDb.update(type,0,2);
                PreferencesUtil.setStringData(mContext,"type", RequestCode.ANDROID_LCD);
                mHandler.sendEmptyMessage(1001);
                return;
            }

            Intent video = new Intent(this, VideoActivity.class);
            video.putExtra("broadType",1);
            video.putExtra("type", type);
            startActivityForResult(video,1001);
        }else if (RequestCode.ANDROID_LCD.equals(type)){
            if(!check(type)){
                MyApplication.getInstance().mDb.update(type,0,2);
                PreferencesUtil.setStringData(mContext,"type", RequestCode.ANDROID_CPU);
                mHandler.sendEmptyMessage(1001);
                return;
            }

            Intent test = new Intent(this, TestActivity.class);
            test.putExtra("broadType",1);
            test.putExtra("type", type);
            startActivityForResult(test,1001);
        } else if (RequestCode.ANDROID_ERROR.equals(type)){
            PreferencesUtil.isFristLogin(this,"first",false);
            PreferencesUtil.setStringData(this,"type","");
            PreferencesUtil.isFristLogin(this,"onClickStart",false);
            mTestResult.setVisibility(View.VISIBLE);
            mTestResult.setText("失败");
            mHandler.sendEmptyMessageDelayed(1004,2000);
        } else if (RequestCode.ANDROID_SUCCESS.equals(type)){
            PreferencesUtil.setStringData(this,"allTime",String.valueOf(Integer.parseInt(PreferencesUtil.getStringData(this,"allTime"))-1));
            if (Integer.parseInt(PreferencesUtil.getStringData(this,"allTime"))==0){
                PreferencesUtil.isFristLogin(this,"onClickStart",false);
                PreferencesUtil.isFristLogin(this,"first",false);
                PreferencesUtil.setStringData(this,"type","");

                mHandler.sendEmptyMessageDelayed(1005,2000);
            }else {
                PreferencesUtil.setStringData(this,"type", RequestCode.ANDROID_EMMC);
                mHandler.sendEmptyMessageDelayed(1001,3000);
            }
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

    private void initListView(){
        mAdapter = new MainAdapter(this,this);
        mLv.setAdapter(mAdapter);
    }

    private void initReboot(String type) {
        if (!PreferencesUtil.getFristLogin(this,"reboot")){
            int time = Integer.parseInt(PreferencesUtil.getStringData(this,"time"));
            PreferencesUtil.setStringData(this,"type",type);

            MyApplication.getInstance().mDb.delete(type);

            TypeModel model = new TypeModel();
            model.setType(type);
            model.setAllTime(time);
            model.setStartTime(DateUtil.getCurrentDate());
            model.setFilepath("");
            model.setIsRun(-1);
            model.setIsPass(0);

            MyApplication.getInstance().mDb.addData(model);
            PreferencesUtil.isFristLogin(this,"reboot",true);

            // 重启
            PowerManager pManager=(PowerManager) getSystemService(Context.POWER_SERVICE);
            pManager.reboot("重启");
        }else{
            TypeModel data = MyApplication.getInstance().mDb.getData(type);

            if (data!=null && !TextUtils.isEmpty(data.getStartTime())){
                if (DateUtil.getTimeInterval(DateUtil.getCurrentDate(),data.getStartTime())<=data.getAllTime()*60){
                    PowerManager pManager=(PowerManager) getSystemService(Context.POWER_SERVICE);
                    pManager.reboot("重启");
                }else{
                    PreferencesUtil.isFristLogin(this,"reboot",false);
                    MyApplication.getInstance().mDb.update(type,0,1);

                    PreferencesUtil.setStringData(this,"type",RequestCode.ANDROID_SUCCESS);

                    mHandler.sendEmptyMessageDelayed(1002,5000);
                }
            }
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
        return true;
    }

    public void register(Context context) {
        IntentFilter intentFilter = new IntentFilter();
        context.registerReceiver(receiver, intentFilter);
    }

    @Override
    public void onOver(final int type) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("提示");
        String str = "";
        if (type == 1){
            str = "是否结束测试?";
        }else{
            str = "是否退出整个测试，重新选择？";
        }
        builder.setMessage(str);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mHandler.removeMessages(1001);
                mHandler.sendEmptyMessage(1003);
                if (type == 2){
                    MyApplication.getInstance().mDb.deleteAll();
                    PreferencesUtil.isFristLogin(mContext,"onClickStart",false);
                    PreferencesUtil.isFristLogin(mContext,"first",false);
                    PreferencesUtil.setStringData(mContext,"type","");

                    CleanMessageUtil.cleanApplicationData(MyApplication.getInstance().getApplicationContext());

                    //退出所有的activity
                    Intent intent = new Intent();
                    intent.setAction(BaseActivity.TAG_ESC_ACTIVITY);
                    sendBroadcast(intent);
                    finish();
                }

            }
        });
        builder.setNegativeButton("取消",null);
        builder.create().show();

    }

    private void append(String key, Object value) {
        resultStringBuffer.append(key + "=" + value +"&");
    }

    private void append1(String key, Object value) {
        resultStringBuffer.append(key + "=" + value);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == 1001){
            initData();
        }
    }
}
