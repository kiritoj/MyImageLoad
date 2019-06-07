package com.example.mifans.myimageload.Interface;

import com.example.mifans.myimageload.Bean.PicBean;



import retrofit2.http.GET;
import retrofit2.http.Url;
import rx.Observable;

public interface PicService {
    @GET
    Observable<PicBean> getPicBean(@Url String url);
}
