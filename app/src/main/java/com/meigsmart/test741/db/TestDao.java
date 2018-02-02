package com.meigsmart.test741.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;


import java.util.ArrayList;
import java.util.List;

/**
 *
 * Created by chenMeng on 2017/11/1.
 */

public class TestDao {

    private DBOpenHelper locationHelper;
    private SQLiteDatabase locationDb;
    public static String TABLE = "test";

    public TestDao(Context context) {
        locationHelper = new DBOpenHelper(context);
    }

    /**
     * 添加一条新记录
     *
     * @param model
     */
    public void addData(TypeModel model) {
        locationDb = locationHelper.getReadableDatabase();

        ContentValues values = new ContentValues();
        values.put("type",model.getType());
        values.put("allTime",model.getAllTime());
        values.put("startTime",model.getStartTime());
        values.put("filepath",model.getFilepath());
        values.put("isRun",model.getIsRun());
        values.put("isPass",model.getIsPass());

        locationDb.insert(TABLE, null, values);
        locationDb.close();
    }

    /**
     * 删除指定记录
     *
     */
    public void delete(String type) {
        locationDb = locationHelper.getReadableDatabase();
        if (locationDb.isOpen())
            locationDb.delete(TABLE, "type=?", new String[]{type});
        locationDb.close();
    }

    /**
     * 更新数据
     */
    public void update(String type,int isRun,int isPass){
        locationDb = locationHelper.getReadableDatabase();
        ContentValues values = new ContentValues();
        if (isRun == 0){
            values.put("startTime","");
        }
        values.put("isRun",isRun);
        values.put("isPass",isPass);
        if (locationDb.isOpen())
            locationDb.update(TABLE,values,"type=?", new String[]{type});
        locationDb.close();
    }

    /**
     * 更新数据
     */
    public void updateRun(String type,int isRun) {
        locationDb = locationHelper.getReadableDatabase();
        ContentValues values = new ContentValues();
        if (isRun == 0){
            values.put("startTime","");
        }
        values.put("isRun",isRun);
        if (locationDb.isOpen())
            locationDb.update(TABLE,values,"type=?", new String[]{type});
        locationDb.close();
    }

    /**
     * 更新数据
     */
    public void updatePass(String type,int isPass) {
        locationDb = locationHelper.getReadableDatabase();
        ContentValues values = new ContentValues();
        values.put("isPass",isPass);
        if (locationDb.isOpen())
            locationDb.update(TABLE,values,"type=?", new String[]{type});
        locationDb.close();
    }

    /**
     * 查询记录  type
     *
     * @return
     */
    public TypeModel getData(String type) {
        TypeModel bean = new TypeModel();

        String sql = "select * from "+TABLE+" where type=?";
        locationDb = locationHelper.getReadableDatabase();
        Cursor cursor = locationDb.rawQuery(sql, new String[]{type});

        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndex("id"));
            int allTime = cursor.getInt(cursor.getColumnIndex("allTime"));
            String startTime = cursor.getString(cursor.getColumnIndex("startTime"));
            String filepath = cursor.getString(cursor.getColumnIndex("filepath"));
            int isRun = cursor.getInt(cursor.getColumnIndex("isRun"));
            int isPass = cursor.getInt(cursor.getColumnIndex("isPass"));

            bean.setId(id);
            bean.setType(type);
            bean.setAllTime(allTime);
            bean.setStartTime(startTime);
            bean.setFilepath(filepath);
            bean.setIsRun(isRun);
            bean.setIsPass(isPass);

        }
        cursor.close();
        //关闭数据库
        locationDb.close();
        return bean;
    }

    /**
     * 查询所有的记录  是否有运行测试的
     *
     * @return
     */
    public boolean checkIsRun() {
        String sql = "select * from "+TABLE;
        locationDb = locationHelper.getReadableDatabase();
        Cursor cursor = locationDb.rawQuery(sql,null);

        while (cursor.moveToNext()) {
            String type = cursor.getString(cursor.getColumnIndex("type"));
            int isRun = cursor.getInt(cursor.getColumnIndex("isRun"));

            Log.e("result","type:"+type+"\nisRun:"+isRun);

            if (isRun == 1){
                return true;
            }

        }
        cursor.close();
        //关闭数据库
        locationDb.close();

        return false;
    }

    /**
     * 查询所有的记录
     *
     * @return
     */
    public List<TypeModel> getAllData() {
        List<TypeModel> dataList = new ArrayList<>();

        String sql = "select * from "+TABLE;
        locationDb = locationHelper.getReadableDatabase();
        Cursor cursor = locationDb.rawQuery(sql,null);

        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndex("id"));
            String type = cursor.getString(cursor.getColumnIndex("type"));
            int allTime = cursor.getInt(cursor.getColumnIndex("allTime"));
            String startTime = cursor.getString(cursor.getColumnIndex("startTime"));
            String filepath = cursor.getString(cursor.getColumnIndex("filepath"));
            int isRun = cursor.getInt(cursor.getColumnIndex("isRun"));
            int isPass = cursor.getInt(cursor.getColumnIndex("isPass"));

            TypeModel model = new TypeModel();
            model.setId(id);
            model.setType(type);
            model.setAllTime(allTime);
            model.setStartTime(startTime);
            model.setFilepath(filepath);
            model.setIsRun(isRun);
            model.setIsPass(isPass);

            dataList.add(model);

        }
        cursor.close();
        //关闭数据库
        locationDb.close();

        return dataList;
    }

}
