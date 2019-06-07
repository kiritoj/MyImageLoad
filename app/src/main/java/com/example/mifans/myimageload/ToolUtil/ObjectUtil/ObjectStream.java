package com.example.mifans.myimageload.ToolUtil.ObjectUtil;

import android.content.Context;

import com.example.mifans.myimageload.Bean.PicBean;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class ObjectStream {
    /**
     * 写入对象
     */
    public static void outputObject(Object o, Context context) {
        FileOutputStream outputStream = null;
        ObjectOutputStream objectOutputStream = null;
        File file = new File(context.getExternalCacheDir().getPath() + File.separator + "object.txt");
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            outputStream = new FileOutputStream(file);
            objectOutputStream = new ObjectOutputStream(outputStream);
            objectOutputStream.writeObject(o);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (objectOutputStream != null) {
                try {
                    objectOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 读入对象
     */
    public static PicBean inputObject(Context context) {
        File file = new File(context.getExternalCacheDir().getPath() + File.separator + "object.txt");
        ObjectInputStream objectInputStream = null;
        FileInputStream inputStream = null;
        PicBean picBean = null;

        try {
            inputStream = new FileInputStream(file);

            objectInputStream = new ObjectInputStream(inputStream);

            picBean = (PicBean) objectInputStream.readObject();
            return picBean;
        } catch (Exception e) {
            e.printStackTrace();
        }


//        } catch (IOException e) {
//            Log.d("error", "inputObject: 错误1");
//        } catch (ClassNotFoundException e) {
//            Log.d("error", "inputObject: 错误2");
        finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (objectInputStream != null) {
                try {
                    objectInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return picBean;
    }
}
