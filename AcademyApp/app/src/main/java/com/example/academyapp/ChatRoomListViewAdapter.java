package com.example.academyapp;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatRoomListViewAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<String> name_list;
    private ArrayList<String> messages;
    private ArrayList<String> profile;

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
        CircleImageView profile_view = (CircleImageView) convertView.findViewById(R.id.profile_view);

        String name = name_list.get(position);
        String message = messages.get(position);
        String photoUrl = profile.get(position);

        name_list_view.setText(name);
        message_view.setText(message);
        if (photoUrl != null) {
            profile_view.setImageURI(Uri.parse(photoUrl));
        }

        return convertView;
    }

}
