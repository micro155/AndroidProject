package com.example.academyapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.logging.LogRecord;

import javax.net.ssl.HttpsURLConnection;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatRoomListViewAdapter extends BaseAdapter {

    Handler handler = new Handler();

    private Context context;
    private ArrayList<String> name_list;
    private ArrayList<String> messages;
    private ArrayList<String> profile;
    private ChattingRoomActivity chattingRoomActivity;

    public ChatRoomListViewAdapter(Context context, ArrayList<String> name_list, ArrayList<String> messages, ArrayList<String> profile) {
        this.context = context;
        this.name_list = name_list;
        this.messages = messages;
        this.profile = profile;
    }

    @Override
    public int getCount() {
        return name_list.size();
    }

    @Override
    public Object getItem(int i) {
        return name_list.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.chatting_room_list_base, parent, false);
        }

        final TextView name_list_view = (TextView) convertView.findViewById(R.id.name_view);
        final TextView message_view = (TextView) convertView.findViewById(R.id.last_chatting_view);
        final CircleImageView profile_view = (CircleImageView) convertView.findViewById(R.id.profile_view);

        String name = name_list.get(position);
        String message = messages.get(position);
        final String photoUrl = profile.get(position);

        name_list_view.setText(name);
        message_view.setText(message);
        if (photoUrl != null) {

            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        URL url = new URL(photoUrl);
                        Log.d("url", "link : " + photoUrl);
                        InputStream is = url.openStream();
                        final Bitmap bm = BitmapFactory.decodeStream(is);
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                profile_view.setImageBitmap(bm);
                            }
                        });
//                        profile_view.setImageBitmap(bm);
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                        Log.d("error log", "log : " + e.getMessage());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            thread.start();
        }
        return convertView;
    }

}
