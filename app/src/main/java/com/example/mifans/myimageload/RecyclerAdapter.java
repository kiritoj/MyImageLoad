package com.example.mifans.myimageload;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.mifans.myimageload.Bean.PicBean;
import com.example.mifans.myimageload.ImageLoader.MyImageLoader;

import java.util.List;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {
    private PicBean picBean;
    private List<PicBean.ResultsBean> resultsBeans;
    private Context context;
    private boolean isloadPic = true;//是否加载图片
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
        final String url = resultsBeans.get(i).getUrl();
        String tag = (String) viewHolder.imageView.getTag();
        if (!url.equals(tag)) {
            viewHolder.imageView.setImageResource(R.drawable.loading);
        }
        if (isloadPic) {

            //滑动时不加载图片，设置默认图片，减缓卡顿
            MyImageLoader.with(context).into(viewHolder.imageView).placeholder(R.drawable.loading).load(url);
        } else {
            viewHolder.imageView.setImageResource(R.drawable.loading);
        }
        //点击查看大图
        viewHolder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context,ShowPicActivity.class);
                intent.putExtra("url",url);
                context.startActivity(intent);

            }
        });


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

    public void setIsloadPic(boolean isload) {
        this.isloadPic = isload;
    }
}
