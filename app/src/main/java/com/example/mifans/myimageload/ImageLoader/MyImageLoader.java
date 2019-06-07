package com.example.mifans.myimageload.ImageLoader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.annotation.DrawableRes;
import android.util.Log;
import android.util.LruCache;
import android.widget.ImageView;


import com.example.mifans.myimageload.Interface.LoadPic;
import com.example.mifans.myimageload.R;
import com.jakewharton.disklrucache.DiskLruCache;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ThreadFactory;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class MyImageLoader {
    private Context context;
    private LruCache<String, Bitmap> lruCache;//内存缓存
    private static DiskLruCache diskLruCache;//磁盘缓存
    private ImageRizer imageRizer = new ImageRizer();//压缩类
    private static boolean PLACEHOLDER = false;//占位图标志符
    private ImageView imageView;
    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final ThreadFactory THREAD_FACTORY = new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r);
        }
    };

//    //线程池，网络获取图片
//    private static final ThreadPoolExecutor EXECUTOR = new ThreadPoolExecutor(CPU_COUNT + 1
//            , 2 * CPU_COUNT + 1
//            , 10
//            , TimeUnit.SECONDS
//            , new LinkedBlockingDeque<Runnable>()
//            , THREAD_FACTORY);
//    private Handler handler = new Handler(Looper.getMainLooper()) {
//        @Override
//        public void handleMessage(Message msg) {
//            LoadResult loadResult = (LoadResult) msg.obj;
//            ImageView imageView = loadResult.imageView;
//            ;
//            Bitmap bitmap = loadResult.bitmap;
//            //检查Tag是否改变，没有改变就设置Bitmap，防止错位
//            imageView.setImageBitmap(bitmap);
//
//
//        }
//    };

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

    //加载图片，三级缓存，检查内存缓存，磁盘缓存，最后网络加载
    public void load(String url) {
        load(url, 0, 0);
    }

    public void load(final String url, final int reqHeight, final int reaWidth) {
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
        bitmap = loadBitmapFromDisk(url, reqHeight, reaWidth);
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
            return;
        }

        //如果都没有，从网络加载
        loadPicFromHttp(url,0,0);


//        Runnable runnable = new Runnable() {
//            @Override
//            public void run() {
//                Bitmap bitmap = loadPicFromHttp(url,0,0);
//                LoadResult result = new LoadResult(imageView,url,bitmap);
//                handler.obtainMessage(1,result).sendToTarget();
//            }
//        };
//        EXECUTOR.execute(runnable);


    }

    /**
     * 如果磁盘缓存和内存缓存都没有，就从网络加载图片并写入磁盘缓存和内存缓存
     *
     * @param url    图片的地址
     * @param height imageview的高
     * @param width  imageview的宽
     * @return
     */
    private void loadPicFromHttp(String url, int height, int width) {
        final String key = hashKeyUrl(url);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://ww1.sinaimg.cn/large/")
                .build();
        LoadPic loadPic = retrofit.create(LoadPic.class);
        Call<ResponseBody> call = loadPic.getPicStream(url);

        //同步请求方式，需使用hander配合更新UI
//        try {
//            Response<ResponseBody>  response= call.execute();
//            if (response.body() != null){
//                InputStream inputStream = response.body().byteStream();
//                bitmap = BitmapFactory.decodeStream(inputStream);
//                imageView.setImageBitmap(bitmap);
//                addBitmapToMem(key, bitmap);
//                try {
//                    DiskLruCache.Editor editor = diskLruCache.edit(key);
//                    if (editor != null) {
//                        OutputStream outputStream = editor.newOutputStream(0);
//                        if (addIntoDiskCache(inputStream, outputStream)) {
//                            editor.commit();
//                        } else {
//                            editor.abort();
//                        }
//                        diskLruCache.flush();
//                    }
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        //异步请求方式,直接在onResponse更新UI
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            //onReponse是在主线程运行的，可以直接更新ui
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.body()!=null) {
                    InputStream inputStream = response.body().byteStream();
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    imageView.setImageBitmap(bitmap);

                    //添加内存缓存
                    addBitmapToMem(key, bitmap);
                    //添加磁盘缓存
                    try {
                        DiskLruCache.Editor editor = diskLruCache.edit(key);
                        if (editor != null) {
                            OutputStream outputStream = editor.newOutputStream(0);
                            if (addIntoDiskCache(inputStream, outputStream)) {
                                editor.commit();
                            } else {
                                editor.abort();
                            }
                            diskLruCache.flush();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }


            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                imageView.setImageResource(R.drawable.error_1);
            }
        });
    }
    //从内存缓存中读取Bitmap
    private Bitmap loadBitmapfromMem(String key) {
        return lruCache.get(key);
    }

    //添加缓存到内存中
    private void addBitmapToMem(String key, Bitmap bitmap) {
        if (lruCache.get(key) == null) {
            lruCache.put(key, bitmap);
        }

    }



    //从磁盘中读出缓存
    private Bitmap loadBitmapFromDisk(String url, int height, int width) {
        Bitmap bitmap = null;
        String key = hashKeyUrl(url);
        try {
            DiskLruCache.Snapshot snapshot = diskLruCache.get(key);
            if (snapshot != null) {
                FileInputStream in = (FileInputStream) snapshot.getInputStream(0);
                FileDescriptor fd = in.getFD();
                //压缩后的Bitmap
                bitmap = imageRizer.decodeFromFileDescriptor(fd, height, width);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    /**
     * 添加磁盘缓存
     *
     * @param inputStream  从网络获取的输入流
     * @param outputStream 磁盘写入editer的输出流
     * @return 写入磁盘缓存是否成功
     */
    private boolean addIntoDiskCache(InputStream inputStream, OutputStream outputStream) {

        BufferedInputStream input = null;
        BufferedOutputStream output = null;
        try {
            input = new BufferedInputStream(inputStream);
            output = new BufferedOutputStream(outputStream);
            int b;
            while ((b = input.read()) != -1) {
                output.write(b);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

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
    public void removeCache() {
        try {
            diskLruCache.delete();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public MyImageLoader placeholder(@DrawableRes int resid) {
        imageView.setImageResource(resid);
        return this;
    }

    public MyImageLoader into(ImageView imageView) {
        this.imageView = imageView;
        return this;
    }
}

//class LoadResult {
//    public ImageView imageView;
//    public String url;
//    public Bitmap bitmap;
//
//    public LoadResult(ImageView imageView, String url, Bitmap bitmap) {
//        this.imageView = imageView;
//        this.url = url;
//        this.bitmap = bitmap;
//    }
//}

