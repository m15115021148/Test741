package com.meigsmart.test741.util;


import android.content.Context;
import android.os.Environment;
import android.os.MemoryFile;
import android.os.ParcelFileDescriptor;
import android.os.storage.StorageManager;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Array;
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
    public static String getStoragePath() {
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
        File file = new File(getStoragePath(),name);
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


    /**
     * 获取sd卡目录  没有 就返回null
     * @param context
     * @param is_removale false 内置sd卡路径   true 外置sd卡路径
     * @return
     */
    public static String getSDPath(Context context, boolean is_removale) {
        StorageManager mStorageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        Class<?> storageVolumeClazz = null;
        try {
            storageVolumeClazz = Class.forName("android.os.storage.StorageVolume");
            Method getVolumeList = mStorageManager.getClass().getMethod("getVolumeList");
            Method getPath = storageVolumeClazz.getMethod("getPath");
            Method isRemovable = storageVolumeClazz.getMethod("isRemovable");
            Object result = getVolumeList.invoke(mStorageManager);
            final int length = Array.getLength(result);
            for (int i = 0; i < length; i++) {
                Object storageVolumeElement = Array.get(result, i);
                String path = (String) getPath.invoke(storageVolumeElement);
                boolean removable = (Boolean) isRemovable.invoke(storageVolumeElement);
                if (is_removale == removable) {
                    return path;
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 创建文件夹
     * @param filePath
     * @return
     */
    public static File createFolder(String filePath){
        File file = new File(filePath+"/HP");
        if (file.exists()){
            return file;
        }else{
            file.mkdir();
            return file;
        }
    }


    /**
     * 保存写入的文件到sd卡目录下
     *
     * @param fileName
     * @param txt
     */
    public static String writeFile(File path, String fileName, String txt) {
        try {
            File file = new File(path, fileName);
            Log.e("result", "create path:" + file.getPath());
            FileOutputStream fos = new FileOutputStream(file, false);//false每次写入都会替换内容
            byte[] b = txt.trim().getBytes();
            fos.write(b);
            fos.close();
            return file.getPath();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 读取文件的内容
     *
     * @param filePath 文件路径
     * @param fileName 文件名称
     * @return
     */
    public static String readFile(String filePath, String fileName) {
        String result = "";
        FileInputStream fis = null;
        BufferedInputStream bis = null;
        ByteArrayOutputStream bos = null;
        try {
            File file = new File(filePath, fileName);
            if (!file.exists()) {
                return null;
            }
            fis = new FileInputStream(file);
            bis = new BufferedInputStream(fis);
            bos = new ByteArrayOutputStream();
            byte[] b = new byte[1024];
            int len = bis.read(b);
            while (len != -1) {
                len = bis.read(b);
                bos.write(b, 0, b.length);
            }
            result = new String(bos.toByteArray());
            Log.e("result", "read data:" + result);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bos != null)bos.close();
                if (bis != null)bis.close();
                if (fis != null)fis.close();
            }catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

}
