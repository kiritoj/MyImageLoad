package com.example.mifans.myimageload;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import rx.Subscriber;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {
    private PicBean picBean;
    private List<PicBean.ResultsBean> resultsBeans;
    private Context context;
    private boolean isload = true;//是否加载图片
    MyImageLoader imageLoader;

    public RecyclerAdapter(PicBean picBean, Context context) {

        this.picBean = picBean;
        this.context = context;
        imageLoader = MyImageLoader.with(context);
        resultsBeans = picBean.getResults();

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item, viewGroup, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        String url = resultsBeans.get(i).getUrl();


//        if (isload) {
           imageLoader.into(viewHolder.imageView).placeholder(R.drawable.loading).load(url);
            //Glide.with(context).load(url).into(viewHolder.imageView);
//        } else {
//            viewHolder.imageView.setImageResource(R.drawable.timg);
//        }


    }

    @Override
    public int getItemCount() {
        return resultsBeans.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_item);

        }
    }

//    public void setIsload(boolean isload) {
//        this.isload = isload;
//    }
}
