package com.example.mifans.myimageload;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.example.mifans.myimageload.ImageLoader.MyImageLoader;

public class ShowPicActivity extends AppCompatActivity {

    ImageView bigPic;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_pic);
        bigPic = findViewById(R.id.big_pic);
        bigPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        Intent intent = getIntent();
        String url = intent.getStringExtra("url");
        MyImageLoader.with(this).into(bigPic).placeholder(R.drawable.loading).load(url);

    }

}
