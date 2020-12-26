package com.example.uber.Remote;

import com.example.uber.Model.FCMResponse;
import com.example.uber.Model.FCMSendData;

import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface IFCMService {
    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAAwt0A-W0:APA91bF9lziA-032kBtI-hfOmjUHstaByWfT0Y-n5_Ig6ySaU7l74-INMxbOZC1QaB-mMYYVuZ8-56yaMfQzI-E0M1YyQVm0Nd0g1e_YGT--pYP8SqWgUZ5oay_nEiKKl4lhuoC8PW9E"
    })
    @POST("fcm/send")
    Observable<FCMResponse> sendNotification(@Body FCMSendData body);
}
