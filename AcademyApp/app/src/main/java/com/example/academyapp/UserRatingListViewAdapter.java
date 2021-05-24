package com.example.academyapp;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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
    private String academy_name;

    public UserRatingListViewAdapter(Context context, ArrayList<String> user_profile_list, ArrayList<String> user_name_list, ArrayList<String> user_rating_list, ArrayList<String> user_text_list, String academy_name) {
        this.context = context;
        this.user_profile_list = user_profile_list;
        this.user_name_list = user_name_list;
        this.user_rating_list = user_rating_list;
        this.user_text_list = user_text_list;
        this.academy_name = academy_name;
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

        DatabaseReference user_ref = FirebaseDatabase.getInstance().getReference(Common.MEMBER_INFO_REFERENCE);
        final DatabaseReference academy_ref = FirebaseDatabase.getInstance().getReference(Common.ACADEMY_INFO_REFERENCE);
        String Uid = FirebaseAuth.getInstance().getCurrentUser().getUid();


        user_name = user_name_list.get(position);
        user_rating = user_rating_list.get(position);
        user_text = user_text_list.get(position);
        user_profile = user_profile_list.get(position);

        if (user_name != null && user_rating != null && user_text != null && user_profile != null) {

            user_name_view.setText(user_name);
            user_rating_view.setText(user_rating);
            user_text_view.setText(user_text);
            user_profile_view.setImageURI(Uri.parse(user_profile));


            final View second_convertView = convertView;
            user_ref.child(Uid).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    final String normal_name = snapshot.child("nickName").getValue(String.class);

                    if (normal_name == user_name) {
                        Button delete_user_text = (Button) second_convertView.findViewById(R.id.delete_user_text);

                        delete_user_text.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                academy_ref.child(academy_name).child("user_rating_info").child(normal_name).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(context, "댓글 삭제 완료", Toast.LENGTH_SHORT).show();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(context, "댓글 삭제 실패", Toast.LENGTH_SHORT).show();
                                        Log.d("reply delete error", "error_code : " + e.getMessage());
                                    }
                                });
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        }

        return convertView;
    }

}
