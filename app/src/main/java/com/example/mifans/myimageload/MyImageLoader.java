package com.example.mifans.myimageload;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.util.LruCache;
import android.widget.ImageView;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MyImageLoader {
    private Context context;
    private LruCache<String, Bitmap> lruCache;//内存缓存
    private DiskLruCache diskLruCache;//磁盘缓存
    private ImageRizer imageRizer = new ImageRizer();//压缩类
    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final ThreadFactory THREAD_FACTORY = new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r);
        }
    };
    //线程池，网络获取图片
    private static final ThreadPoolExecutor EXECUTOR = new ThreadPoolExecutor(CPU_COUNT+1
            ,2*CPU_COUNT+1
            ,10
            ,TimeUnit.SECONDS
            ,new LinkedBlockingDeque<Runnable>()
            ,THREAD_FACTORY);
    private Handler handler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            LoadResult loadResult = (LoadResult) msg.obj;
            ImageView imageView = loadResult.imageView;;
            Bitmap bitmap = loadResult.bitmap;
            //检查Tag是否改变，没有改变就设置Bitmap，防止错位
            if (imageView.getTag().equals(loadResult.url)){
                imageView.setImageBitmap(bitmap);
            }

        }
    };

    //实例化imageloader以及创建内存缓存和磁盘缓存
    private MyImageLoader(Context context) {
        this.context = context.getApplicationContext();
        int ramUsable = (int) (Runtime.getRuntime().maxMemory() / 1024);//当前可用内存，单位KB
        int cachememory = ramUsable / 8;//内存缓存大小
        //创建内存缓存
        lruCache = new LruCache<String, Bitmap>(cachememory) {
            @Override
            //bitmap的大小
            protected int sizeOf(String key, Bitmap bitmap) {
                int result = bitmap.getRowBytes() * bitmap.getHeight() / 1024;
                Log.d("test", String.valueOf(result));
                return result;
            }
        };
        //创建磁盘缓存
        File director = getDirecyor(this.context, "Bitmap");
        if (!director.exists()) {
            director.mkdirs();
        }
        try {
            //设置50MB的缓存大小
            diskLruCache = DiskLruCache.open(director, 1, 1, 1024 * 1024 * 20);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static MyImageLoader with(Context context) {
        return new MyImageLoader(context);
    }

    //从内存缓存中读取Bitmap
    private Bitmap loadBitmapfromMem(String url) {
        String key = hashKeyUrl(url);
        return lruCache.get(key);
    }

    //添加缓存到内存中
    private void addBitmapToMem(String url, Bitmap bitmap) {
        String key = hashKeyUrl(url);
        if (loadBitmapfromMem(url) == null) {
            lruCache.put(key, bitmap);
        }
    }

    //添加缓存到磁盘缓存中,需要从网络请求数据并写入，还要添加到内存缓存中,height和width是imageView的大小
    private Bitmap addBitmapToDiskFromHttp(String url,int height,int width) {
        Bitmap bitmap;
        String key = hashKeyUrl(url);
        try {
            DiskLruCache.Editor editor = diskLruCache.edit(key);
            if (editor != null){
                OutputStream outputStream = editor.newOutputStream(0);
                if (downloadBitmapFromHttp(url,outputStream)){
                    editor.commit();
                }else {
                    editor.abort();
                }
                diskLruCache.flush();
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
        bitmap = loadBitmapFromDisk(url,height,width);
        if (bitmap!=null) {
            addBitmapToMem(url, bitmap);
        }
        return bitmap;
    }

    //从磁盘中读出缓存
    private Bitmap loadBitmapFromDisk(String url,int height,int width){
        Bitmap bitmap = null;
        String key = hashKeyUrl(url);
        try {
            DiskLruCache.Snapshot snapshot = diskLruCache.get(key);
            if (snapshot != null){
                FileInputStream in = (FileInputStream) snapshot.getInputStream(0);
                FileDescriptor fd = in.getFD();
                //压缩后的Bitmap
                bitmap = imageRizer.decodeFromFile(fd,height,width);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }
    private boolean downloadBitmapFromHttp(String url, OutputStream outputStream) {
        HttpURLConnection connection = null;
        BufferedInputStream input = null;
        BufferedOutputStream output = null;
        try {
            URL url1 = new URL(url);
            connection = (HttpURLConnection) url1.openConnection();
            input = new BufferedInputStream(connection.getInputStream());
            output = new BufferedOutputStream(outputStream);
            int b;
            while ((b = input.read()) != -1) {
                output.write(b);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }

            try {
                if (input != null) {
                    input.close();
                }
                if (output != null) {
                    output.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }



    //加载图片，三级缓存，检查内存缓存，磁盘缓存，最后网络加载
    public void load(String url, ImageView imageView) {
        load(url, imageView, 0, 0);
    }

    public void load(final String url, final ImageView imageView, final int reqHeight, final int reaWidth) {
        //设置tag，防止图片出现错位
        imageView.setTag(url);
        Bitmap bitmap;
        //首先检查内存缓存
        bitmap = loadBitmapfromMem(url);
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
            return;
        }
        //然后检查磁盘缓存
        bitmap = loadBitmapFromDisk(url,reqHeight,reaWidth);
        if (bitmap != null){
            imageView.setImageBitmap(bitmap);
            return;
        }
        //如果都没有，开启线程取网络获取
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap1 = addBitmapToDiskFromHttp(url,reqHeight,reaWidth);
                LoadResult result = new LoadResult(imageView,url,bitmap1);
                handler.obtainMessage(1,result).sendToTarget();
            }
        };
        EXECUTOR.execute(runnable);
    }

    //缓存的key加密
    private String hashKeyUrl(String url) {
        String cacheKey;
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(url.getBytes());
            cacheKey = ByteHexToString(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            cacheKey = String.valueOf(url.hashCode());
        }
        return cacheKey;
    }

    private String ByteHexToString(byte[] bytes) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1) {
                stringBuilder.append('0');
            }
            stringBuilder.append(hex);
        }
        return stringBuilder.toString();
    }

    //获取磁盘缓存路径
    private File getDirecyor(Context context, String name) {
        String cachePath;
        //如果手机SD存在或不可被移除，返回路径 /sdcard/Android/data/<application package>/cache
        //否则返回磁盘缓存路径 /data/data/<application package>/cache
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            cachePath = context.getExternalCacheDir().getPath();
        } else {
            cachePath = context.getCacheDir().getPath();
        }
        return new File(cachePath + File.separator + name);
    }
    //清除缓存
    public void removeCache(){
        try {
            diskLruCache.delete();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
class LoadResult{
    public ImageView imageView;
    public String url;
    public Bitmap bitmap;

    public LoadResult(ImageView imageView, String url,Bitmap bitmap) {
        this.imageView = imageView;
        this.url = url;
        this.bitmap = bitmap;
    }
}

