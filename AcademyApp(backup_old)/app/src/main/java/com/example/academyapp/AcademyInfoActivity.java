package com.example.academyapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.academyapp.Model.Rating_Info;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class AcademyInfoActivity extends AppCompatActivity {

    private ImageView academy_image;
    private CircleImageView academy_profile;
    private TextView academy_name;
    private TextView academy_phone;
    private TextView academy_address;
    private ListView user_rating_listView;
    private Spinner user_rating_input_spinner;
    private EditText edit_user_text;
    private String academy;
    private UserRatingListViewAdapter adapter;
    private String[] rating_items = {"1.0 / 5.0", "2.0 / 5.0", "3.0 / 5.0", "4.0 / 5.0", "5.0 / 5.0"};
    private String user_rating_input;
    private Button btn_user_rating;
//    private String user_text_input;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_academy_info);

        Intent intent = getIntent();
        academy = intent.getExtras().getString("academy_name");

        Log.d("academy_value", "value : " + academy);

//        Toolbar toolbar = findViewById(R.id.toolbar_academy_info);
//        setSupportActionBar(toolbar);
//        getSupportActionBar().setTitle(academy);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        academy_image = (ImageView) findViewById(R.id.academy_imageView);
        academy_profile = (CircleImageView) findViewById(R.id.academy_profile_info);
        academy_name = (TextView) findViewById(R.id.academy_name_info);
        academy_phone = (TextView) findViewById(R.id.academy_phone_info);
        academy_address = (TextView) findViewById(R.id.academy_address_info);
        user_rating_listView = (ListView) findViewById(R.id.user_rating_list);
        user_rating_input_spinner = (Spinner) findViewById(R.id.user_rating_input);
        edit_user_text = (EditText) findViewById(R.id.edit_user_text);
        btn_user_rating = (Button) findViewById(R.id.btn_user_rating);


        showDetailAcademy(academy);

        ArrayAdapter<String> spinner_adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, rating_items);

        user_rating_input_spinner.setAdapter(spinner_adapter);
        user_rating_input_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                user_rating_input = rating_items[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        btn_user_rating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final DatabaseReference academy_ref = FirebaseDatabase.getInstance().getReference(Common.ACADEMY_INFO_REFERENCE);
                DatabaseReference user_ref = FirebaseDatabase.getInstance().getReference(Common.MEMBER_INFO_REFERENCE);
                String user_auth = FirebaseAuth.getInstance().getCurrentUser().getUid();

                user_ref.child(user_auth).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String user_name = snapshot.child("nickName").getValue(String.class);

                        Uri user_profile_uri = FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl();

                        Rating_Info model = new Rating_Info();

                        model.setUser_profile(String.valueOf(user_profile_uri));
                        model.setUser_name(user_name);
                        model.setUser_rating(user_rating_input);
                        model.setUser_text(edit_user_text.getText().toString());

                        academy_ref.child(academy).child("user_rating_info").setValue(model);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }
        });

    }

    private void showDetailAcademy(final String academy) {

        final Handler handler = new Handler();

        final ArrayList<String> user_profile_list = new ArrayList<>();
        final ArrayList<String> user_name_list = new ArrayList<>();
        final ArrayList<String> user_rating_list = new ArrayList<>();
        final ArrayList<String> user_text_list = new ArrayList<>();

        DatabaseReference academy_info = FirebaseDatabase.getInstance().getReference(Common.ACADEMY_INFO_REFERENCE);

        academy_info.child(academy).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                final String image_name = snapshot.child("academy_image").getValue(String.class);
                String address = snapshot.child("academy_address").getValue(String.class);
                String tel = snapshot.child("academy_tel").getValue(String.class);
                final String profile_url = snapshot.child("director_photo_url").getValue(String.class);

                Glide.with(AcademyInfoActivity.this).load("https://firebasestorage.googleapis.com/v0/b/academyapp-d7c41.appspot.com/o/academy_images%2F" + image_name + "?alt=media")
                        .into(academy_image);

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            URL url = new URL(String.valueOf(profile_url));
                            Log.d("url string", "url : " + url);

                            URLConnection conn = url.openConnection();
                            conn.connect();
                            BufferedInputStream bis = new BufferedInputStream(conn.getInputStream());
                            final Bitmap bm = BitmapFactory.decodeStream(bis);
                            bis.close();
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    academy_profile.setImageBitmap(bm);
                                }
                            });
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();

                academy_name.setText(academy);
                academy_phone.setText(tel);
                academy_address.setText(address);

                for (DataSnapshot rating_snapshot : snapshot.getChildren()) {
                    String user_name = rating_snapshot.child("user_name").getValue(String.class);
                    String user_profile = rating_snapshot.child("user_profile").getValue(String.class);
                    String user_text = rating_snapshot.child("user_text").getValue(String.class);
                    String user_rating = rating_snapshot.child("user_rating").getValue(String.class);

                    if (user_name != null && user_profile != null && user_text != null && user_rating != null) {

                        Log.d("user_rating", "user name : " + user_name + ", user profile : " + user_profile + ", " + "user text : " + user_text + ", " + "user rating : " + user_rating);

                        user_profile_list.add(user_profile);
                        user_name_list.add(user_name);
                        user_text_list.add(user_text);
                        user_rating_list.add(user_rating);

                    }

                }

                adapter = new UserRatingListViewAdapter(AcademyInfoActivity.this, user_profile_list, user_name_list, user_rating_list, user_text_list, academy, new UserRatingListViewAdapter.OnRatingDeleteListener() {
                    @Override
                    public void onRatingDelete(String normal_name) {
                        adapter.deleteButtonAction(normal_name);
                    }
                });
                adapter.notifyDataSetChanged();

                user_rating_listView.setAdapter(adapter);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:{ //toolbar의 back키 눌렀을 때 동작
                finish();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

}