package com.example.academyapp.RestAPI;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitConnection {

    String URL = "https://naveropenapi.apigw.ntruss.com";

//    private String id = "z79q0dob9r";
//    private String key = "l7JKaTHv8v4CE3wV5xc7G8exQs3HQ61y8n0ajNr3";
//
//
//    OkHttpClient client = new OkHttpClient.Builder()
//            .addInterceptor(new Interceptor() {
//                @Override
//                public Response intercept(Chain chain) throws IOException {
//                    Request newRequest  = chain.request().newBuilder()
//                            .addHeader("X-NCP-APIGW-API-KEY-ID", id)
//                            .addHeader("X-NCP-APIGW-API-KEY", key)
//                            .build();
//                    return chain.proceed(newRequest);
//                }
//            }).build();

    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build();

    public MapAPI mapAPI = retrofit.create(MapAPI.class);
}
