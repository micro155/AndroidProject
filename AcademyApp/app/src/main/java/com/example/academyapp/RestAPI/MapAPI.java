package com.example.academyapp.RestAPI;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;

public interface MapAPI {
    @Headers({
            "X-NCP-APIGW-API-KEY-ID:z79q0dob9r",
            "X-NCP-APIGW-API-KEY:l7JKaTHv8v4CE3wV5xc7G8exQs3HQ61y8n0ajNr3"
    })
    @GET("/map-geocode/v2/geocode")
    Call<RequestAddress> getCoordinate(@Query("query") String location);
}
