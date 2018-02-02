package com.meigsmart.test741.util;


import android.content.Context;
import android.content.SharedPreferences;


public class PreferencesUtil {
	/** log标志 */
	private static final String TAG = "tag";
	/** 是否第一次登陆preferneces标志 */
	private static final String SP_LOGIN_PRIVATE = "sp_login_private";
	/** 保存实体类中preferneces标志 */
	private static final String SP_MODEL_PRIVATE = "sp_model_private";

	/**
	 * 是否第一个登录 判断导航页面
	 * 
	 * @param context
	 *            本类
	 * @param key
	 *            保存的标志
	 * @param isFrist
	 *            true为是第一次登录
	 * @see #getFristLogin(Context, String) 配套使用
	 * @return
	 */
	public static void isFristLogin(Context context, String key, boolean isFrist) {
		SharedPreferences sp = context.getSharedPreferences(SP_LOGIN_PRIVATE,
				Context.MODE_PRIVATE);
		sp.edit().putBoolean(key, isFrist).apply();
	}

	/**
	 * 获取第一个登录保存的内容
	 * 
	 * @param context
	 *            本类
	 * @param key
	 *            保存的标志
	 * @return
	 */
	public static boolean getFristLogin(Context context, String key) {
		SharedPreferences sp = context.getSharedPreferences(SP_LOGIN_PRIVATE,
				Context.MODE_PRIVATE);
		return sp.getBoolean(key, false);
	}

	/**
	 * 清除保存的实体类
	 * 
	 * @param context
	 *            本类
	 * @param key
	 *            保存的标志
	 * @return
	 */
	public static boolean deleteDataModel(Context context, String key) {
		SharedPreferences sp = context.getSharedPreferences(SP_MODEL_PRIVATE,
				Context.MODE_PRIVATE);
		return sp.edit().putString(key,"").commit();
	}

	/**
	 * 清除保存的实体类
	 *
	 * @param context
	 *            本类
	 * @param key
	 *            保存的标志
	 * @return
	 */
	public static boolean deleteDataFrist(Context context, String key) {
		SharedPreferences sp = context.getSharedPreferences(SP_LOGIN_PRIVATE,
				Context.MODE_PRIVATE);
		return sp.edit().putBoolean(key,false).commit();
	}

	/**
	 * 保存一个string类型
	 * @param context
	 * @param key
	 */
	public static void setStringData(Context context, String key,String values){
		SharedPreferences sp = context.getSharedPreferences(SP_LOGIN_PRIVATE,
				Context.MODE_PRIVATE);
		sp.edit().putString(key,values).apply();
	}

	/**
	 * 得到保存的字符串
	 * @param context
	 * @param key
	 * @return
	 */
	public static String getStringData(Context context, String key){
		SharedPreferences sp = context.getSharedPreferences(SP_LOGIN_PRIVATE,
				Context.MODE_PRIVATE);
		return sp.getString(key,"");
	}
}
