package com.example.academyapp.RestAPI;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;

public interface MapAPI {
    @Headers({
            "X-NCP-APIGW-API-KEY-ID:",
            "X-NCP-APIGW-API-KEY:"
    })
    @GET("/map-geocode/v2/geocode")
    Call<GeocodingResponse> getCoordinate(@Query("query") String location);
}
