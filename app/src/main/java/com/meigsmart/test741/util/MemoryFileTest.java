package com.meigsmart.test741.util;

import android.os.MemoryFile;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by chenMeng on 2018/1/27.
 */

public class MemoryFileTest {
    private OnMemoryReadAndWriteBack callBack;

    public interface OnMemoryReadAndWriteBack{
        void onFailure();
        void onSuccess();
    }

    public MemoryFileTest(OnMemoryReadAndWriteBack content) {
        this.callBack = content;
    }

    public void testMemoryReadAndWrite(String filePath) {
        try {
            byte[] msg = getBytes(filePath);
            MemoryFile file = new MemoryFile("MemoryFileTest", 100000000);
            byte[] buffer = new byte[msg.length];

            file.writeBytes(msg, 0, 2000, msg.length);
            file.readBytes(buffer, 2000, 0, msg.length);

            compareBuffers(msg, buffer, msg.length);


            OutputStream os = file.getOutputStream();
            os.write(msg);

            InputStream is = file.getInputStream();
            is.mark(msg.length);
            buffer = new byte[msg.length];
            is.read(buffer);
            compareBuffers(msg, buffer, msg.length);

            is.reset();
            buffer = new byte[msg.length];
            is.read(buffer);
            compareBuffers(msg, buffer, msg.length);
            file.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (callBack!=null)callBack.onSuccess();
        }
    }

    private void compareBuffers(byte[] buffer1, byte[] buffer2, int length) {
        for (int i = 0; i < length; i++) {
            if (buffer1[i] != buffer2[i]) {
                if (callBack!=null)callBack.onFailure();
            }
        }
    }

    /**
     * 获得指定文件的byte数组
     */
    private byte[] getBytes(String filePath) {
        byte[] buffer = null;
        try {
            File file = new File(filePath);
            FileInputStream fis = new FileInputStream(file);
            ByteArrayOutputStream bos = new ByteArrayOutputStream(1000);
            byte[] b = new byte[1000];
            int n;
            while ((n = fis.read(b)) != -1) {
                bos.write(b, 0, n);
            }
            fis.close();
            bos.close();
            buffer = bos.toByteArray();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return buffer;
    }
}
