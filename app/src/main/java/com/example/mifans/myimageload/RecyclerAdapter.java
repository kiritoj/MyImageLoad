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
    private List<PictureData> pictureDataList = new ArrayList<>();
    private Context context;
    MyImageLoader imageLoader;
    public RecyclerAdapter(List<PictureData> pictureDataList,Context context) {
        this.pictureDataList = pictureDataList;
        this.context = context;
        imageLoader = MyImageLoader.with(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item,viewGroup,false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        PictureData data = pictureDataList.get(i);
        viewHolder.textView.setText(data.getWho());
        imageLoader.load(data.getUrl(), viewHolder.imageView);
        //MyImageLoader.with(context).load(data.getUrl(),viewHolder.imageView);这样会抛异常java.lang.IllegalStateException: edit didn't create file 0
    }

    @Override
    public int getItemCount() {
        return pictureDataList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView textView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_item);
            textView = itemView.findViewById(R.id.who_tv);
        }
    }
}
