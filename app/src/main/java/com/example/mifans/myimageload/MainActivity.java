package com.example.mifans.myimageload;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.UrlQuerySanitizer;
import android.os.Looper;
import android.renderscript.ScriptGroup;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    RecyclerAdapter adapter;
    List<PictureData> pictureDataList = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
        recyclerView = findViewById(R.id.recycler_view);
        HttpUtil.sendHttpRequest("http://gank.io/api/data/%E7%A6%8F%E5%88%A9/0/0", new HttpUtilListener() {
            @Override
            public void success(String response) {
                parseJSONWithJSONObject(response);
            }

            @Override
            public void failed() {
                Looper.prepare();
                Toast.makeText(MainActivity.this,"好像出了一点问题",Toast.LENGTH_SHORT).show();
            }
        });
        StaggeredGridLayoutManager manager = new StaggeredGridLayoutManager(2
                ,StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(manager);
        adapter = new RecyclerAdapter(pictureDataList,this);
        recyclerView.setAdapter(adapter);
//        ImageView imageView = findViewById(R.id.test_iv);
//        MyImageLoader.with(this).load("http://ww2.sinaimg.cn/large/7a8aed7bjw1ewym3nctp0j20i60qon23.jpg",imageView);



    }
    public void parseJSONWithJSONObject(String JSONData){
        try {
            JSONObject jsonObject = new JSONObject(JSONData);
            JSONArray jsonArray = jsonObject.getJSONArray("results");

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObjectitem = jsonArray.getJSONObject(i);
                String who = jsonObjectitem.getString("who");
                String url = jsonObjectitem.getString("url");
                PictureData data = new PictureData(who,url);
                pictureDataList.add(0,data);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.remove_cache:
                MyImageLoader.with(this).removeCache();
                Toast.makeText(MainActivity.this,"已清除全部缓存",Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
        return true;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(MainActivity.this, "拒绝授权将无法使用本应用哦", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }
}
