package com.example.academyapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
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

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserRatingListViewAdapter extends BaseAdapter {

    private Handler handler = new Handler();

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
    private OnRatingDeleteListener rating_deleteClickListener;

    public interface OnRatingDeleteListener {
        void onRatingDelete (String normal_name);
    }

    public UserRatingListViewAdapter(Context context, ArrayList<String> user_profile_list, ArrayList<String> user_name_list, ArrayList<String> user_rating_list, ArrayList<String> user_text_list, String academy_name, OnRatingDeleteListener rating_deleteClickListener) {
        this.context = context;
        this.user_profile_list = user_profile_list;
        this.user_name_list = user_name_list;
        this.user_rating_list = user_rating_list;
        this.user_text_list = user_text_list;
        this.academy_name = academy_name;
        this.rating_deleteClickListener = rating_deleteClickListener;
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

        final CircleImageView user_profile_view = (CircleImageView) convertView.findViewById(R.id.user_profile_info);
        TextView user_name_view = (TextView) convertView.findViewById(R.id.normal_name_info);
        TextView user_rating_view = (TextView) convertView.findViewById(R.id.user_rating_info);
        TextView user_text_view = (TextView) convertView.findViewById(R.id.user_text_info);

        user_name = user_name_list.get(position);
        user_rating = user_rating_list.get(position);
        user_text = user_text_list.get(position);
        user_profile = user_profile_list.get(position);

        if (user_name != null && user_rating != null && user_text != null && user_profile != null) {

            user_name_view.setText(user_name);
            user_rating_view.setText(user_rating);
            user_text_view.setText(user_text);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        URL url = new URL(user_profile);
                        Log.d("url string", "url : " + String.valueOf(url));

                        URLConnection conn = url.openConnection();
                        conn.connect();
                        BufferedInputStream bis = new BufferedInputStream(conn.getInputStream());
                        final Bitmap bm = BitmapFactory.decodeStream(bis);
                        bis.close();
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                user_profile_view.setImageBitmap(bm);
                            }
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();

//            delete_user_text.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    rating_deleteClickListener.onRatingDelete(user_name);
//                }
//
//            });

            DatabaseReference user_ref = FirebaseDatabase.getInstance().getReference(Common.MEMBER_INFO_REFERENCE);
            final String Uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

            final View second_convertView = convertView;
            user_ref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        String uid = dataSnapshot.child("uid").getValue(String.class);

                        if (uid != null) {
                            if (uid.equals(Uid)) {
                                final String normal_name = dataSnapshot.child("nickName").getValue(String.class);

                                Button delete_user_text = (Button) second_convertView.findViewById(R.id.btn_delete_user_text);

                                delete_user_text.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        rating_deleteClickListener.onRatingDelete(normal_name);
                                    }

                                });

                            }
                        }
                    }


                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });



        }

        return convertView;
    }

    public void deleteButtonAction(final String normal_name) {

        final DatabaseReference academy_ref = FirebaseDatabase.getInstance().getReference(Common.ACADEMY_INFO_REFERENCE);

        AlertDialog.Builder alert_builder = new AlertDialog.Builder(context);
        alert_builder.setTitle("댓글 삭제")
                .setMessage("댓글을 삭제하시겠습니까?")
                .setPositiveButton("예", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        academy_ref.child(academy_name).child("user_rating_info").child(normal_name).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                user_name_list.remove(normal_name);
                                user_rating_list.remove(user_rating);
                                user_text_list.remove(user_text);
                                user_profile_list.remove(user_profile);
                                setItemList(user_name_list, user_rating_list, user_text_list, user_profile_list);

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
                }).setNegativeButton("아니오", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(context, "삭제 취소", Toast.LENGTH_SHORT).show();
            }
        });

        AlertDialog alertDialog = alert_builder.create();

        alertDialog.show();

    }

    public void setItemList(ArrayList<String> user_name_list, ArrayList<String> user_rating_list, ArrayList<String> user_text_list, ArrayList<String> user_profile_list) {
        this.user_name_list = user_name_list;
        this.user_rating_list = user_rating_list;
        this.user_text_list = user_text_list;
        this.user_profile_list = user_profile_list;
        notifyDataSetChanged();
    }

}
