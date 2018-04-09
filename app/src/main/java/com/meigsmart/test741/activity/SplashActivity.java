package com.meigsmart.test741.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.meigsmart.test741.R;
import com.meigsmart.test741.config.RequestCode;
import com.meigsmart.test741.model.SplashModel;
import com.meigsmart.test741.util.DeviceUtil;
import com.meigsmart.test741.util.FileUtil;
import com.meigsmart.test741.util.PreferencesUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class SplashActivity extends BaseActivity implements View.OnClickListener {
    private SplashActivity mContext;
    private ListView mLv;
    private int[] times = {3, 4, 6, 12, 24};
    private List<SplashModel> mList = new ArrayList<>();
    private SplashAdapter adapter;
    private TextView mStart;
    private boolean isSelect;
    private int currPos = 0;
    private TextView mGetLog;
    private StringBuffer resultStringBuffer = new StringBuffer();
    private String mData = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        mContext = this;
        mLv = findViewById(R.id.listView);
        mStart = findViewById(R.id.start);
        mStart.setOnClickListener(this);
        mGetLog = findViewById(R.id.include).findViewById(R.id.over);
        mGetLog.setText("GetLog");

        if (PreferencesUtil.getFristLogin(this, "first")) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        for (int i = 0; i < times.length; i++) {
            SplashModel model = new SplashModel();
            model.setSelect(0);
            model.setTime(times[i]);
            mList.add(model);
        }

        adapter = new SplashAdapter(this, mList);
        mLv.setAdapter(adapter);
        mLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                isSelect = true;
                currPos = position;
                for (SplashModel m : mList) {
                    m.setSelect(0);
                }
                mStart.setSelected(true);
                mList.get(position).setSelect(1);
                adapter.notifyDataSetChanged();
            }
        });

        mData = FileUtil.readFile(FileUtil.getStoragePath()+"/HP", RequestCode.SAVE_TEST_RESULT);

        if (TextUtils.isEmpty(mData)){
            mGetLog.setVisibility(View.GONE);
        }else{
            mGetLog.setVisibility(View.VISIBLE);
            mGetLog.setOnClickListener(this);
        }

    }

    @Override
    public void onClick(View v) {
        if (v == mStart) {
            if (isSelect) {
                PreferencesUtil.isFristLogin(this,"onClickStart",true);
                PreferencesUtil.isFristLogin(this, "first", true);
                PreferencesUtil.setStringData(this, "allTime", String.valueOf(mList.get(currPos).getTime()));
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "请选择测试时间", Toast.LENGTH_SHORT).show();
            }
        }
        if (v == mGetLog){
            saveData();
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

    private void saveData(){
        resultStringBuffer.setLength(0);
        String str = TextUtils.isEmpty(FileUtil.getSDPath(mContext, true))?"（请插入sd卡）":"";
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("提示");
        builder.setMessage(Html.fromHtml("是否获取上次测试结果日志？"+"<font color='#ff0000'>"+str+"</font>"));
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
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

                String objectJson = getObjectJson(mData.trim(), getAllResult(mData.trim()) , productionCode, staffCode);

                String path = FileUtil.writeFile(FileUtil.createFolder(FileUtil.getStoragePath()),RequestCode.SAVE_RESULT_PATH,objectJson);

                Toast.makeText(mContext,"路径："+path,Toast.LENGTH_SHORT).show();

            }
        });
        builder.setNegativeButton("取消", null);
        //builder.setCancelable(false);
        builder.create().show();
    }

    private boolean getAllResult(String data) {
        if (TextUtils.isEmpty(data))return false;

        if (data.contains("Failure") || data.contains("NO TEST") || !data.contains("Success")){
            return false;
        }

        return true;
    }

    private String getObjectJson(String result,boolean type,String ws,String empCode){
        append(RequestCode.JSON_SN, Build.SERIAL);
        append(RequestCode.JSON_MACADDR, DeviceUtil.getMac());
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

    private void append(String key, Object value) {
        resultStringBuffer.append(key + "=" + value +"&");
    }

    private void append1(String key, Object value) {
        resultStringBuffer.append(key + "=" + value);
    }

}
