package com.example.academyapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

public class AcademyListViewAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<String> name_list;
    private ArrayList<String> address_list;
    private String academy_name;
    private String academy_address;

    public interface OnListClickListener {
        void onDownload (String academyName);
    }

    public AcademyListViewAdapter(Context context, ArrayList<String> name_list, ArrayList<String> address_list) {
        this.context = context;
        this.name_list = name_list;
        this.address_list = address_list;
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
            convertView = inflater.inflate(R.layout.search_academy_list, parent, false);
        }

        final TextView name_view = (TextView) convertView.findViewById(R.id.list_academy_name);
        final TextView address_view = (TextView) convertView.findViewById(R.id.list_academy_address);

        academy_name = name_list.get(position);
        academy_address = address_list.get(position);

        name_view.setText(academy_name);
        address_view.setText(academy_address);


        return convertView;
    }

}
