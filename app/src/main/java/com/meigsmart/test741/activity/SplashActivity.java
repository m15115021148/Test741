package com.meigsmart.test741.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.meigsmart.test741.R;
import com.meigsmart.test741.model.SplashModel;
import com.meigsmart.test741.util.PreferencesUtil;

import java.util.ArrayList;
import java.util.List;


public class SplashActivity extends BaseActivity implements View.OnClickListener {
    private ListView mLv;
    private int[] times = {3, 4, 6, 12, 24};
    private List<SplashModel> mList = new ArrayList<>();
    private SplashAdapter adapter;
    private TextView mStart;
    private boolean isSelect;
    private int currPos = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        mLv = findViewById(R.id.listView);
        mStart = findViewById(R.id.start);
        mStart.setOnClickListener(this);


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

}
