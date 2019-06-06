package com.example.mifans.myimageload.Interface;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

/**
 * 获取图片的输入流
 */
public interface LoadPic {
    @GET
    Call<ResponseBody> getPicStream(@Url String url);
}
