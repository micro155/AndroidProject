package com.example.academyapp.RestAPI;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface MapAPI {
    @GET("/map-geocode/v2/geocode")
    Call<RequestAddress> getCoordinate(@Query("query") String address);
}
