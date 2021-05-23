package com.example.academyapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class UserRatingListViewAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<String> user_profile_list;
    private ArrayList<String> user_name_list;
    private ArrayList<String> user_rating_list;
    private ArrayList<String> user_text_list;
    private String user_name;
    private String user_rating;
    private String user_profile;
    private String user_text;

    public UserRatingListViewAdapter(Context context, ArrayList<String> user_profile_list, ArrayList<String> user_name_list, ArrayList<String> user_rating_list, ArrayList<String> user_text_list) {
        this.context = context;
        this.user_profile_list = user_profile_list;
        this.user_name_list = user_name_list;
        this.user_rating_list = user_rating_list;
        this.user_text_list = user_text_list;
    }

    @Override
    public int getCount() {
        return user_name_list.size();
    }

    @Override
    public Object getItem(int i) {
        return user_name_list.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.academy_rating_list_base, parent, false);
        }

        ImageView user_profile_view = (ImageView) convertView.findViewById(R.id.user_profile_info);
        TextView user_name_view = (TextView) convertView.findViewById(R.id.normal_name_info);
        TextView user_rating_view = (TextView) convertView.findViewById(R.id.user_rating_info);
        TextView user_text_view = (TextView) convertView.findViewById(R.id.user_text_info);

        user_name = user_name_list.get(position);
        user_rating = user_rating_list.get(position);
        user_text = user_text_list.get(position);
        user_profile = user_profile_list.get(position);

        user_name_view.setText(user_name);
        user_rating_view.setText(user_rating);
        user_text_view.setText(user_text);
//        user_profile_view.setImageURI();



        return convertView;
    }

}
