package com.meigsmart.test741;

import android.app.Application;
import android.content.Context;

import com.meigsmart.test741.db.TestDao;

/**
 * Created by chenMeng on 2018/1/30.
 */

public class MyApplication extends Application {
    private static MyApplication instance;// application对象
    public TestDao mDb;//数据库

    @Override
    public void onCreate() {
        super.onCreate();
        mDb = new TestDao(this);
    }

    public static MyApplication getInstance(){
        return instance;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        instance = this;
    }

}
