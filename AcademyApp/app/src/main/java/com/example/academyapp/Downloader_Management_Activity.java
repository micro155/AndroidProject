package com.example.academyapp;

import androidx.annotation.NonNull;
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
import android.opengl.Visibility;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Downloader_Management_Activity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 100;

    private DrawerLayout drawer;
    private NavigationView navigationView;
    private AppBarConfiguration mAppBarConfiguration;
    private NavController navController;

    private AlertDialog waitingDialog;
    private StorageReference storageReference;
    private ImageView img_profile;
    private Uri imageUri;

    private ListView listView;
    private DatabaseReference downloader_ref;
    private DatabaseReference user_ref;
    private String academy_url;
    private DownloaderListViewAdapter adapter;
    private TextView empty_text;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_downloader_management);

        Toolbar toolbar = findViewById(R.id.toolbar_downloader_management);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.menu_downloader_management);

        drawer = findViewById(R.id.drawer_director_downloader_management);

        navigationView = findViewById(R.id.nav_director_downloader_management_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_director_home, R.id.nav_director_logout, R.id.nav_upload, R.id.nav_academy_management, R.id.nav_chatting_director, R.id.nav_downloader_management_director)
                .setDrawerLayout(drawer)
                .build();
        navController = Navigation.findNavController(this, R.id.nav_downloader_management_director_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        navigationView.setCheckedItem(R.id.nav_downloader_management_director);

        empty_text = (TextView) findViewById(R.id.empty_list_tag);
        listView = (ListView) findViewById(R.id.downloader_list_view);
        downloader_ref = FirebaseDatabase.getInstance().getReference("Contracts");
        user_ref = FirebaseDatabase.getInstance().getReference(Common.ACADEMY_INFO_REFERENCE);
        academy_url = FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl().toString();

        init();

        user_ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {

                    String photo_url = dataSnapshot.child("director_photo_url").getValue(String.class);
                    Log.d("director_url", "director url string : " + photo_url);

                    if (photo_url != null) {
                        if (photo_url.equals(academy_url)) {
                            String academy_name = dataSnapshot.child("academy_name").getValue(String.class);
                            showDownloaderList(academy_name);
                        }
                    }

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void showDownloaderList(final String academy_name) {

        final ArrayList<String> downloader_nickName_list = new ArrayList<String>();
        final ArrayList<String> downloader_phone_list = new ArrayList<String>();

        if (academy_name != null) {

            downloader_ref.child(academy_name).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    boolean check = false;

                    for (DataSnapshot downloader : snapshot.getChildren()) {
                        final String downloader_name = downloader.child("downloader_name").getValue(String.class);
                        final String downloader_phone = downloader.child("downloader_phone").getValue(String.class);
                        final String academy_code = downloader.child("academy_code").getValue(String.class);
                        final String downloader_nickName = downloader.child("downloader_nickName").getValue(String.class);

                        if (downloader_name != null) {
                            check = true;

                            downloader_nickName_list.add(downloader_nickName);
                            downloader_phone_list.add(downloader_phone);

                            adapter = new DownloaderListViewAdapter(Downloader_Management_Activity.this, downloader_nickName_list, downloader_phone_list, academy_name, downloader_name, academy_code, new DownloaderListViewAdapter.OnDeleteClickListener() {
                                @Override
                                public void onDelete(String downloader_nickName, String academy_name) {
                                    adapter.deleteDownloader(downloader_nickName, academy_name);
                                }
                            });

                            adapter.notifyDataSetChanged();
                            listView.setAdapter(adapter);
                        }

//                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//                        @Override
//                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//
//                            Log.d("Button click", "click OK");
//
//                            AlertDialog.Builder detail_dialog = new AlertDialog.Builder(Downloader_Management_Activity.this);
//                            View detail_info_view = LayoutInflater.from(Downloader_Management_Activity.this).inflate(R.layout.layout_detail_downloader_info, null);
//
//                            TextView downloader_nickName_view = (TextView) detail_info_view.findViewById(R.id.downloader_detail_nickName);
//                            TextView downloader_name_view = (TextView) detail_info_view.findViewById(R.id.downloader_detail_downloader_name);
//                            TextView downloader_phone_view = (TextView) detail_info_view.findViewById(R.id.downloader_detail_downloader_phone);
//                            TextView downloader_academy_code_view = (TextView) detail_info_view.findViewById(R.id.downloader_detail_academy_code);
//
//                            Button downloader_detail_confirm_button = (Button) detail_info_view.findViewById(R.id.downloader_detail_confirm);
//
//                            downloader_nickName_view.setText(downloader_name);
//                            downloader_name_view.setText(downloader_phone);
//                            downloader_phone_view.setText(academy_code);
//                            downloader_academy_code_view.setText(downloader_nickName);
//
//                            detail_dialog.setView(detail_info_view);
//
//                            final AlertDialog dialog = detail_dialog.create();
//                            dialog.show();
//
//                            downloader_detail_confirm_button.setOnClickListener(new View.OnClickListener() {
//                                @Override
//                                public void onClick(View v) {
//                                    dialog.dismiss();
//                                }
//                            });
//
//                        }
//                    });

                    }

                    if (!check) {
                        empty_text.setVisibility(View.VISIBLE);
                    } else {
                        empty_text.setVisibility(View.INVISIBLE);
                    }

//                adapter.notifyDataSetChanged();
//                listView.setAdapter(adapter);


                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        }

    }

    private void init() {
        waitingDialog = new AlertDialog.Builder(this)
                .setCancelable(false)
                .setMessage("잠시만 기다려주세요.")
                .create();

        storageReference = FirebaseStorage.getInstance().getReference();

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.nav_director_logout) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(Downloader_Management_Activity.this);
                    builder.setTitle("로그아웃")
                            .setMessage("정말 로그아웃 하시겠습니까?")
                            .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int which) {
                                    dialogInterface.dismiss();
                                }
                            })
                            .setPositiveButton("로그아웃", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    FirebaseAuth.getInstance().signOut();
                                    Intent intent = new Intent(Downloader_Management_Activity.this, SplashScreenActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                    finish();
                                }
                            })
                            .setCancelable(false);
                    final AlertDialog dialog = builder.create();
                    dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                        @Override
                        public void onShow(DialogInterface dialogInterface) {
                            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                                    .setTextColor(ContextCompat.getColor(Downloader_Management_Activity.this, android.R.color.holo_red_dark));
                            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                                    .setTextColor(ContextCompat.getColor(Downloader_Management_Activity.this, R.color.colorAccent));
                        }
                    });
                    dialog.show();
                } else if (item.getItemId() == R.id.nav_upload) {
                    Intent intent = new Intent(Downloader_Management_Activity.this, UploadActivity.class);
                    startActivity(intent);
                    finish();
                } else if (item.getItemId() == R.id.nav_academy_management) {
                    Intent intent = new Intent(Downloader_Management_Activity.this, AcademyManagementActivity.class);
                    startActivity(intent);
                    finish();
                } else if (item.getItemId() == R.id.nav_director_home) {
                    Intent intent = new Intent(Downloader_Management_Activity.this, DirectorHomeActivity.class);
                    startActivity(intent);
                    finish();
                } else if (item.getItemId() == R.id.nav_chatting_director) {
                    Intent intent = new Intent(Downloader_Management_Activity.this, ChattingRoom_Director_Activity.class);
                    startActivity(intent);
                    finish();
                } else if (item.getItemId() == R.id.nav_downloader_management_director) {
                    Intent intent = new Intent(Downloader_Management_Activity.this, Downloader_Management_Activity.class);
                    startActivity(intent);
                    finish();
                }
                return false;
            }
        });

        View headerView = navigationView.getHeaderView(0);
        final TextView txt_nick_name = (TextView)headerView.findViewById(R.id.txt_nick_name);
        final TextView txt_email = (TextView)headerView.findViewById(R.id.txt_email);
        img_profile = (ImageView)headerView.findViewById(R.id.img_profile);

        DatabaseReference user_ref = FirebaseDatabase.getInstance().getReference(Common.MEMBER_INFO_REFERENCE);
        final String user_uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        final String user_email = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        user_ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String uid = dataSnapshot.child("uid").getValue(String.class);

                    if (uid != null && uid.equals(user_uid)) {
                        String nickName = dataSnapshot.child("nickName").getValue(String.class);
                        txt_nick_name.setText(nickName);
                        txt_email.setText(user_email);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        img_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, PICK_IMAGE_REQUEST);
            }
        });

        if (Common.currentMember != null && Common.currentMember.getProfile() != null && !TextUtils.isEmpty(Common.currentMember.getProfile())) {
            Glide.with(this)
                    .load(Common.currentMember.getProfile())
                    .into(img_profile);
        }
    }

    private void showDialogUpload() {
        final DatabaseReference normal_ref = FirebaseDatabase.getInstance().getReference(Common.MEMBER_INFO_REFERENCE);
        final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        AlertDialog.Builder builder = new AlertDialog.Builder(Downloader_Management_Activity.this);
        builder.setTitle("프로필 변경")
                .setMessage("정말로 프로필을 변경하시겠습니까?")
                .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        dialogInterface.dismiss();
                    }
                })
                .setPositiveButton("업로드", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        if (imageUri != null) {
                            waitingDialog.setMessage("업로드중...");
                            waitingDialog.show();

                            String unique_name = FirebaseAuth.getInstance().getCurrentUser().getUid();
                            final StorageReference profileFolder = storageReference.child("profiles/" + unique_name);

                            profileFolder.putFile(imageUri)
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            waitingDialog.dismiss();
                                            Snackbar.make(drawer, e.getMessage(), Snackbar.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                            if (task.isSuccessful()) {
                                                profileFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                    @Override
                                                    public void onSuccess(Uri uri) {
                                                        final Map<String, Object> updateData = new HashMap<>();
                                                        updateData.put("profile", uri.toString());

                                                        normal_ref.addValueEventListener(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                                                    String user_id = dataSnapshot.child("uid").getValue(String.class);

                                                                    if (user_id != null) {
                                                                        if (user_id.equals(uid)) {
                                                                            String nick_name = dataSnapshot.child("nickName").getValue(String.class);
                                                                            UserUtils.updateUser(drawer, updateData, nick_name);
                                                                        }
                                                                    }
                                                                }
                                                            }

                                                            @Override
                                                            public void onCancelled(@NonNull DatabaseError error) {

                                                            }
                                                        });
                                                    }
                                                });
                                            }
                                            waitingDialog.dismiss();
                                        }
                                    }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                                    double progress = (100.0 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                                    waitingDialog.setMessage(new StringBuilder("업로드 : ").append(progress).append("%"));
                                }
                            });
                        }
                    }
                })
                .setCancelable(false);
        final AlertDialog dialog = builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                        .setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                        .setTextColor(getResources().getColor(R.color.colorAccent));
            }
        });
        dialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            if(data != null && data.getData() != null) {
                imageUri = data.getData();
                img_profile.setImageURI(imageUri);

                showDialogUpload();
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_downloader_management_director_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }


}