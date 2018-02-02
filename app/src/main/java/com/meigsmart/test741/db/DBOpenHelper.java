package com.meigsmart.test741.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBOpenHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "MeiGsmartTest741_02.db"; //数据库名称
    private static final int DB_VERSION = 1;//数据库版本,大于0

    //用于聊天表
    private static final String CREATE_LOCATION = "create table "+TestDao.TABLE+" ("
            + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
            + "type TEXT, "
            + "allTime int, "
            + "startTime TEXT, "
            + "isRun int, "
            + "isNormal int, "
            + "isPass int, "
            +"filepath TEXT)";

    public DBOpenHelper(Context context) {
        super(context, DB_NAME, null,DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_LOCATION);//执行有更改的sql语句
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
