package com.example.academyapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.academyapp.Utils.UserUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class AcademyInfoActivity extends AppCompatActivity {

    private ImageView academy_image;
    private CircleImageView academy_profile;
    private TextView academy_name;
    private TextView academy_phone;
    private TextView academy_address;
    private ListView user_rating_list;
    private Spinner user_rating;
    private EditText edit_user_text;
    private String academy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_academy_info);

        Intent intent = getIntent();
        academy = intent.getExtras().getString("academy_name");

        Toolbar toolbar = findViewById(R.id.toolbar_downloader_management);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(academy);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        academy_image = (ImageView) findViewById(R.id.academy_imageView);
        academy_profile = (CircleImageView) findViewById(R.id.academy_profile_info);
        academy_name = (TextView) findViewById(R.id.academy_name_info);
        academy_phone = (TextView) findViewById(R.id.academy_phone_info);
        academy_address = (TextView) findViewById(R.id.academy_address_info);
        user_rating_list = (ListView) findViewById(R.id.user_rating_list);
        user_rating = (Spinner) findViewById(R.id.user_rating_input);
        edit_user_text = (EditText) findViewById(R.id.edit_user_text);


        showDetailAcademy(academy);

    }

    private void showDetailAcademy(String academy) {

        DatabaseReference academy_info = FirebaseDatabase.getInstance().getReference(Common.ACADEMY_INFO_REFERENCE);

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