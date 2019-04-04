package com.example.mifans.myimageload;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
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
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    RecyclerAdapter adapter;
    List<String> pictureDataList = new ArrayList<>();
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        progressDialog = new ProgressDialog(this);
        recyclerView = findViewById(R.id.recycler_view);
        String JSONdata = loadJSONdata();
        if (TextUtils.isEmpty(JSONdata)){
            progressDialog.setTitle("第一次加载时间会稍长一点");
            progressDialog.setMessage("loading");
            progressDialog.setCancelable(false);
            progressDialog.show();
            HttpUtil.sendHttpRequest("http://gank.io/api/data/%E7%A6%8F%E5%88%A9/0/0", new HttpUtilListener() {
                @Override
                public void success(String response) {

                    parseJSONWithJSONObject(response);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adapter.notifyDataSetChanged();
                        }
                    });
                    progressDialog.dismiss();
                    saveJSONdata(response);

                }

                //
                @Override
                public void failed() {
                    Looper.prepare();
                    Toast.makeText(MainActivity.this, "好像出了一点问题", Toast.LENGTH_SHORT).show();
                }
            });
        }else {
            parseJSONWithJSONObject(JSONdata);
        }

        StaggeredGridLayoutManager manager = new StaggeredGridLayoutManager(2
                , StaggeredGridLayoutManager.VERTICAL);

        recyclerView.setLayoutManager(manager);
        adapter = new RecyclerAdapter(pictureDataList, this);
        recyclerView.setAdapter(adapter);
        //添加recycleview滚动监听
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState != RecyclerView.SCROLL_STATE_IDLE) {
                    adapter.setIsload(false);
                    adapter.notifyDataSetChanged();
//                    Glide.with(MainActivity.this).resumeRequests();
                } else {
                    adapter.setIsload(true);
                    adapter.notifyDataSetChanged();
//                    Glide.with(MainActivity.this).pauseRequests();
                }
            }
        });


    }

    public void parseJSONWithJSONObject(String JSONData) {
        try {
            JSONObject jsonObject = new JSONObject(JSONData);
            JSONArray jsonArray = jsonObject.getJSONArray("results");

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObjectitem = jsonArray.getJSONObject(i);
                String url = jsonObjectitem.getString("url");
                pictureDataList.add(0,url);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.remove_cache:
                MyImageLoader.with(this).removeCache();
                Toast.makeText(MainActivity.this, "已清除全部缓存", Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
        return true;
    }

    //将第一次获取到的json文本保存下来
    private void saveJSONdata(String response) {
        FileOutputStream out = null;
        BufferedWriter writer = null;
        try {
            out = openFileOutput("JSONdata", Context.MODE_PRIVATE);
            writer = new BufferedWriter(new OutputStreamWriter(out));
            writer.write(response);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {

            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    //从文件读取json信息
    public String loadJSONdata(){
        FileInputStream  in = null;
        BufferedReader reader = null;
        StringBuilder builder = new StringBuilder();
        try {
            in = openFileInput("JSONdata");
            if (in != null) {
                reader = new BufferedReader(new InputStreamReader(in));
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
                return builder.toString();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (reader != null){
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
       return null;
    }
}
