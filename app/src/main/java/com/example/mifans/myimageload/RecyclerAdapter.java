package com.example.mifans.myimageload;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {
    private List<String> urllist = new ArrayList<>();
    private Context context;
    private boolean isload = true;//是否加载图片

    public RecyclerAdapter(List<String> urllistt, Context context) {
        this.urllist = urllistt;
        this.context = context;
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
        String url = urllist.get(i);


        if (isload) {
            MyImageLoader.with(context).into(viewHolder.imageView).placeholder(R.drawable.timg).load(url);
        } else {
            viewHolder.imageView.setImageResource(R.drawable.timg);
        }


    }

    @Override
    public int getItemCount() {
        return urllist.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_item);

        }
    }

    public void setIsload(boolean isload) {
        this.isload = isload;
    }
}
