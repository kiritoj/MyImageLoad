package com.example.mifans.myimageload;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.mifans.myimageload.Bean.PicBean;
import com.example.mifans.myimageload.ImageLoader.MyImageLoader;
import com.example.mifans.myimageload.Interface.PicService;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import com.example.mifans.myimageload.ToolUtil.ObjectUtil.*;

public class MainActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    RecyclerAdapter adapter;
    ProgressDialog progressDialog;
    PicBean mpicBean;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        progressDialog = new ProgressDialog(this);
        recyclerView = findViewById(R.id.recycler_view);
        progressDialog.setTitle("第一次加载时间会稍长一点");
        progressDialog.setMessage("loading");
        progressDialog.setCancelable(false);



        StaggeredGridLayoutManager manager = new StaggeredGridLayoutManager(2
                , StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(manager);

        //第一次加载图片以后只需从文件读取保存图片url的PicBean对象
        if ((mpicBean = ObjectStream.inputObject(this)) != null) {
            adapter = new RecyclerAdapter(mpicBean, MainActivity.this);
            recyclerView.setAdapter(adapter);

        } else {
            progressDialog.show();
            //retrofit+rxjava方式解析json
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("http://gank.io/api/data/%E7%A6%8F%E5%88%A9/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .build();
            PicService picService = retrofit.create(PicService.class);
            picService.getPicBean("http://gank.io/api/data/%E7%A6%8F%E5%88%A9/0/0")
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Subscriber<PicBean>() {
                        @Override
                        public void onCompleted() {

                        }

                        @Override
                        public void onError(Throwable e) {

                        }

                        @Override
                        public void onNext(PicBean picBean) {
                            adapter = new RecyclerAdapter(picBean, MainActivity.this);
                            recyclerView.setAdapter(adapter);
                            progressDialog.dismiss();
                            //写入picbean到文件，下次不用再从网络获取
                            ObjectStream.outputObject(picBean, MainActivity.this);
                        }
                    });


            //添加recycleview滚动监听
            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);

                    //只有屏幕停止下来才加载图片，使用体验非常糟糕。。。我还不会其他解决方案，菜++
                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        adapter.setIsloadPic(true);
                        adapter.notifyDataSetChanged();

                    } else {

                        adapter.setIsloadPic(false);
                        adapter.notifyDataSetChanged();
                    }
                }
            });

        }
    }

    /**
     * 菜单
     *
     * @param menu
     * @return
     */
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



}
