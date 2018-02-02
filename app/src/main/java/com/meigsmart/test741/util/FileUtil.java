package com.meigsmart.test741.util;


import android.os.Environment;
import android.os.MemoryFile;
import android.os.ParcelFileDescriptor;
import android.text.TextUtils;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DecimalFormat;

/**
 * Created by chenMeng on 2018/1/26.
 */

public class FileUtil {

    /**
     * 获取内置SD卡路径
     * @return
     */
    private static String getInnerSDCardPath() {
        return Environment.getExternalStorageDirectory().getPath();
    }

    /**
     * 获取内部data更目录
     * @return
     */
    private static String getSystemRoot(){
        return Environment.getDataDirectory().getPath();
    }

    public static String createSDPath(String name){
        File file = new File(getInnerSDCardPath(),name);
        return file.getPath();
    }

    public static String createInnerPath(String name){
        File file = new File(getSystemRoot(),name);
        return file.getPath();
    }


    /**
     * 读取文件的大小
     */

    public long getFileSize(File f) throws Exception {
        long l = 0;
        if (f.exists()) {
            FileInputStream is = new FileInputStream(f);
            l = is.available();
        }
        return l;
    }


    /**
     * 转换文件大小
     *
     * @param fileS
     * @return
     */
    public static String formatFileSize(long fileS) {
        DecimalFormat df = new DecimalFormat("#.00");
        String fileSizeString = "";
        String wrongSize = "0B";
        if (fileS == 0) {
            return wrongSize;
        }
        if (fileS < 1024) {
            fileSizeString = df.format((double) fileS) + "B";
        } else if (fileS < 1048576) {
            fileSizeString = df.format((double) fileS / 1024) + "KB";
        } else if (fileS < 1073741824) {
            fileSizeString = df.format((double) fileS / 1048576) + "MB";
        } else {
            fileSizeString = df.format((double) fileS / 1073741824) + "GB";
        }
        return fileSizeString;
    }

}
