package com.example.mifans.myimageload;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.FileDescriptor;

public class ImageRizer {
    private static final String TAG = "ImageRizer";

    public ImageRizer() {
    }
    public Bitmap decodeFromResource(Resources resources,int resID,int outHeight,int outWidth){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(resources,resID,options);
        options.inSampleSize = getInsampleSize(options,outHeight,outWidth);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(resources,resID,options);
    }
    public Bitmap decodeFromFile(FileDescriptor fd,int outHeight,int outWidth){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFileDescriptor(fd,null,options);
        options.inSampleSize = getInsampleSize(options,outHeight,outWidth);
        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeFileDescriptor(fd,null,options);
    }
    public int getInsampleSize(BitmapFactory.Options options,int outheight,int outwidth){
        if (outheight == 0 || outwidth == 0){
            return 1;
        }
        int height = options.outHeight;
        int width = options.outWidth;
        Log.d(TAG,"原始图片大小为"+height+"*"+width);
        int inSampleSize = 1;
        if (height > outheight || width > outwidth){
            height = height/2;
            width = width/2;
        }
        while (height/inSampleSize > outheight && width/inSampleSize > outwidth){
            inSampleSize *=2;
        }
        Log.d(TAG,"采样率为"+inSampleSize);
        return inSampleSize;
    }
}
