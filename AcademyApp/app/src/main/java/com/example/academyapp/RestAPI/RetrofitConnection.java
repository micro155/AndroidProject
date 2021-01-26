package com.example.academyapp.RestAPI;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitConnection {

    String URL = "https://naveropenapi.apigw.ntruss.com";

    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build();

    public MapAPI mapAPI = retrofit.create(MapAPI.class);
}
