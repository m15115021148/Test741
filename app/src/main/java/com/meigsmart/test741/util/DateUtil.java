package com.meigsmart.test741.util;

import android.annotation.SuppressLint;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by chenMeng on 2018/1/25.
 */

public class DateUtil {

    /**
     * 当前时间 如: 2013-04-22 10:37
     * @return
     */
    @SuppressLint("SimpleDateFormat")
    public static String getCurrentDate() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return format.format(Calendar.getInstance().getTime());
    }

    /**
     * 获取两个日期的时间差yyyy.MM.dd HH.mm
     */
    @SuppressLint("SimpleDateFormat")
    public static int getTimeInterval(String e_date,String s_date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        int interval = 0;
        try {
            Date startTime = dateFormat.parse(s_date);
            Date endTime = dateFormat.parse(e_date);
            interval = (int) ((endTime.getTime() - startTime.getTime()) / (1000));// 时间差 单位秒
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return interval;
    }
}
