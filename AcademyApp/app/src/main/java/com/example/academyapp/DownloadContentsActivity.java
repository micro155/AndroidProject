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
import android.renderscript.Sampler;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
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

public class DownloadContentsActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 100;

    private DrawerLayout drawer;
    private AppBarConfiguration mAppBarConfiguration;
    private NavController navController;
    private NavigationView navigationView;

    private DatabaseReference FileStorage_Ref;
    private DatabaseReference Contracts_Ref;
    private DatabaseReference Normal_Ref;

    private AlertDialog waitingDialog;
    private StorageReference storageReference;
    private ImageView img_profile;
    private Uri imageUri;

    private ListView listView;
    private FileListViewAdapter adapter;

    private FirebaseStorage storage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_contents);

        confirmContractVideo();

        Toolbar toolbar = findViewById(R.id.toolbar_download);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_download_layout);

        navigationView = findViewById(R.id.nav_download_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_normalmember_home, R.id.nav_normalmember_logout, R.id.nav_download, R.id.nav_chatting)
                .setDrawerLayout(drawer)
                .build();
        navController = Navigation.findNavController(this, R.id.nav_download_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        listView = (ListView) findViewById(R.id.file_list_view);

        final ArrayList<String> academy_list = new ArrayList<String>();
        final ArrayList<String> file_list = new ArrayList<String>();

        FileStorage_Ref = FirebaseDatabase.getInstance().getReference("FileList");

        FileStorage_Ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot fileData : snapshot.getChildren()) {
                    String key = fileData.getKey();
                    final String value = fileData.child("file_name").getValue(String.class);

                    DatabaseReference mUid = FirebaseDatabase.getInstance().getReference(Common.ACADEMY_INFO_REFERENCE);

                    mUid.child(key).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String academy = snapshot.child("academy_name").getValue(String.class);

                            academy_list.add(academy);
                            file_list.add(value);

                            Log.d("dataAdapter", "adapter : " + snapshot.getChildren());
                            adapter = new FileListViewAdapter(DownloadContentsActivity.this, file_list, academy_list, new FileListViewAdapter.OnDownloadClickListener() {
                                @Override
                                public void onDownload(String fileName) {
                                    adapter.download_File(fileName);
                                }
                            });
                            adapter.notifyDataSetChanged();
                            listView.setAdapter(adapter);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        init();

    }

    private void confirmContractVideo() {

        Normal_Ref = FirebaseDatabase.getInstance().getReference(Common.MEMBER_INFO_REFERENCE);
        
        Contracts_Ref = FirebaseDatabase.getInstance().getReference("Contracts");

        Normal_Ref.child("nickName").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                final String name = snapshot.getValue(String.class);
                
                Contracts_Ref.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String normal_contracts = snapshot.getValue(String.class);
                        
                        if (name.equals(normal_contracts)) {
                            showDownloadContentsList(name);
                        } else {
                            RegisterContractVideo();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void RegisterContractVideo() {

        AlertDialog.Builder registerDialog = new AlertDialog.Builder(this);
//        View registerView = LayoutInflater.from(this).inflate()

    }

    private void showDownloadContentsList(String normal_name) {


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
                    AlertDialog.Builder builder = new AlertDialog.Builder(DownloadContentsActivity.this);
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
                                    Intent intent = new Intent(DownloadContentsActivity.this, SplashScreenActivity.class);
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
                                    .setTextColor(ContextCompat.getColor(DownloadContentsActivity.this, android.R.color.holo_red_dark));
                            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                                    .setTextColor(ContextCompat.getColor(DownloadContentsActivity.this, R.color.colorAccent));
                        }
                    });
                    dialog.show();
                } else if (item.getItemId() == R.id.nav_download) {
                    Intent intent = new Intent(DownloadContentsActivity.this, DownloadContentsActivity.class);
                    startActivity(intent);
                    finish();
                } else if (item.getItemId() == R.id.nav_normalmember_home) {
                    Intent intent = new Intent(DownloadContentsActivity.this, NormalMemberHomeActivity.class);
                    startActivity(intent);
                    finish();
                } else if (item.getItemId() == R.id.nav_chatting) {
                    Intent intent = new Intent(DownloadContentsActivity.this, ChattingRoom_Normal_Activity.class);
                    startActivity(intent);
                    finish();
                }
                return false;
            }
        });

        View headerView = navigationView.getHeaderView(0);
        TextView txt_nick_name = (TextView)headerView.findViewById(R.id.txt_nick_name);
        TextView txt_email = (TextView)headerView.findViewById(R.id.txt_email);
        img_profile = (ImageView)headerView.findViewById(R.id.img_profile);

        txt_nick_name.setText(Common.buildWelcomeMessage());
        txt_email.setText(Common.currentMember != null ? Common.currentMember.getEmail() : "");

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
        AlertDialog.Builder builder = new AlertDialog.Builder(DownloadContentsActivity.this);
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
                                                        Map<String, Object> updateData = new HashMap<>();
                                                        updateData.put("profile", uri.toString());

                                                        UserUtils.updateUser(drawer, updateData);
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
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            if(data != null && data.getData() != null) {
                imageUri = data.getData();
                img_profile.setImageURI(imageUri);

                showDialogUpload();
            }
        }
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.customer_home, menu);
//        return true;
//    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_download_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}