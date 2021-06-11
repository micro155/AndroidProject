package com.example.academyapp;

import android.util.Log;

import org.json.JSONObject;

import needle.Needle;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SendNotification {
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    public static void sendNotification(final String regToken, final String title, final String messsage){
        Needle.onBackgroundThread().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    OkHttpClient client = new OkHttpClient();
                    JSONObject json = new JSONObject();
                    JSONObject dataJson = new JSONObject();
                    dataJson.put("body", messsage);
                    dataJson.put("title", title);
                    json.put("notification", dataJson);
                    json.put("to", regToken);
                    RequestBody body = RequestBody.create(JSON, json.toString());
                    Request request = new Request.Builder()
                            .header("Authorization", "key=" + "AAAAj4TTHAg:APA91bFmGHzBt5Fx0CdKUzQJiNrkvDOQlMQpqzBQm2Uw2jXLFWdlrjW6BnNW3flaDEHdTF_mAu0PcxxSm66OmxZ1pF-WBY7LEc2OdOt0y_ctouLxuddU17Jy2o2n29WH_t_G_cw6Winy")
                            .url("https://fcm.googleapis.com/fcm/send")
                            .post(body)
                            .build();
                    Response response = client.newCall(request).execute();
                    String finalResponse = response.body().string();
                }catch (Exception e){
                    Log.d("error", e+"");
                }
            }
        });


//        new AsyncTask<Void, Void, Void>() {
//            @Override
//            protected Void doInBackground(Void... parms) {
//                try {
//                    OkHttpClient client = new OkHttpClient();
//                    JSONObject json = new JSONObject();
//                    JSONObject dataJson = new JSONObject();
//                    dataJson.put("body", messsage);
//                    dataJson.put("title", title);
//                    json.put("notification", dataJson);
//                    json.put("to", regToken);
//                    RequestBody body = RequestBody.create(JSON, json.toString());
//                    Request request = new Request.Builder()
//                            .header("Authorization", "key=" + "AKXY5Wc7gWvZf5tEj")
//                            .url("https://fcm.googleapis.com/fcm/send")
//                            .post(body)
//                            .build();
//                    Response response = client.newCall(request).execute();
//                    String finalResponse = response.body().string();
//                }catch (Exception e){
//                    Log.d("error", e+"");
//                }
//                return  null;
//            }
//        }.execute();
    }
}
