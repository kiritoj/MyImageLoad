package com.example.mifans.myimageload;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.FileDescriptor;
import java.io.InputStream;

public class ImageRizer {
    private static final String TAG = "ImageRizer";

    public ImageRizer() {
    }

    /**
     * 从资源文件获取图并压缩
     *
     * @param resources
     * @param resID
     * @param outHeight
     * @param outWidth
     * @return
     */
    public Bitmap decodeFromResource(Resources resources, int resID, int outHeight, int outWidth) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(resources, resID, options);
        options.inSampleSize = getInsampleSize(options, outHeight, outWidth);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(resources, resID, options);
    }

    /**
     * 从磁盘缓存获取fileinputstream，用于替代decodeStream这种特殊情况
     *
     * @param fd
     * @param outHeight
     * @param outWidth
     * @return
     */
    public Bitmap decodeFromFileDescriptor(FileDescriptor fd, int outHeight, int outWidth) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFileDescriptor(fd, null, options);
        options.inSampleSize = getInsampleSize(options, outHeight, outWidth);
        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeFileDescriptor(fd, null, options);
    }

    /**
     * 压缩从网络上获取的inputStream
     */
    public Bitmap decodeFromInputStream(InputStream inputStream,int outHeight,int outWidth){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(inputStream,null,options);
        options.inSampleSize = getInsampleSize(options,outHeight,outWidth);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeStream(inputStream,null,options);
    }
    /**
     * 计算采样率
     *
     * @param options
     * @param outheight
     * @param outwidth
     * @return
     */
    public int getInsampleSize(BitmapFactory.Options options, int outheight, int outwidth) {
        if (outheight == 0 || outwidth == 0) {
            return 1;
        }
        int height = options.outHeight;
        int width = options.outWidth;
        Log.d(TAG, "原始图片大小为" + height + "*" + width);
        int inSampleSize = 1;
        if (height > outheight || width > outwidth) {
            height = height / 2;
            width = width / 2;
        }
        while (height / inSampleSize > outheight && width / inSampleSize > outwidth) {
            inSampleSize *= 2;
        }
        Log.d(TAG, "采样率为" + inSampleSize);
        return inSampleSize;
    }
}
