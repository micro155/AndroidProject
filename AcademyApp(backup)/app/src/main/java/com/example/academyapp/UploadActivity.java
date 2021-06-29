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
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.stream.MediaStoreVideoThumbLoader;
import com.example.academyapp.Model.MemberInfoModel;
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

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class UploadActivity extends AppCompatActivity {

    private static final String TAG = "UploadActivity";
    private static final int PICK_IMAGE_REQUEST = 100;

    private AppBarConfiguration mAppBarConfiguration;
    private DrawerLayout drawer;
    private NavigationView navigationView;
    private NavController navController;

    private AlertDialog waitingDialog;
    private StorageReference storageReference;
    private ImageView img_profile;
    private Uri imageUri;

    private Button btChoose;
    private Button btUpload;
    private EditText upload_file_name;
//    private ImageView ivPreview;
    private String mUid;
    private ListView listView;
    private DirectorFileListViewAdapter adapter;
    private TextView empty_upload;

    private Uri filePath;
    DatabaseReference FileDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        Toolbar toolbar = findViewById(R.id.toolbar_upload);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.menu_upload);

        drawer = findViewById(R.id.drawer_upload_layout);
        navigationView = findViewById(R.id.nav_upload_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_director_home, R.id.nav_director_logout, R.id.nav_upload, R.id.nav_academy_management, R.id.nav_chatting_director, R.id.nav_downloader_management_director)
                .setDrawerLayout(drawer)
                .build();
        navController = Navigation.findNavController(this, R.id.nav_upload_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        final DatabaseReference user_ref = FirebaseDatabase.getInstance().getReference(Common.ACADEMY_INFO_REFERENCE);
        final DatabaseReference file_ref = FirebaseDatabase.getInstance().getReference("FileList");
        final Uri photo_url = FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl();

        init();

        btChoose = (Button) findViewById(R.id.bt_choose);
        btUpload = (Button) findViewById(R.id.bt_upload);
        upload_file_name = (EditText) findViewById(R.id.upload_file);
        listView = (ListView) findViewById(R.id.video_list);
        empty_upload = (TextView) findViewById(R.id.empty_upload);

        //버튼 클릭 이벤트
        btChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //이미지를 선택s
                Intent intent = new Intent();
                intent.setType("video/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "동영상을 선택하세요."), 0);
            }
        });

        btUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //업로드

                user_ref.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        for (DataSnapshot user_profile_snapshot : snapshot.getChildren()) {
                            String director_profile = user_profile_snapshot.child("director_photo_url").getValue(String.class);

                            if (String.valueOf(photo_url).equals(director_profile)) {
                                String academy_name = user_profile_snapshot.child("academy_name").getValue(String.class);
                                String upload_name = upload_file_name.getText().toString();
                                if (upload_name.isEmpty()) {
                                    Toast.makeText(getApplicationContext(), "파일 이름을 입력하세요.", Toast.LENGTH_SHORT).show();
                                    return;
                                } else {
                                    uploadFile(academy_name, upload_name);
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

        final ArrayList<String> file_list = new ArrayList<String>();

        user_ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot profileSnapshot : snapshot.getChildren()) {
                    String profile_url = profileSnapshot.child("director_photo_url").getValue(String.class);

                    if (String.valueOf(photo_url).equals(profile_url)) {
                        final String academy_name = profileSnapshot.child("academy_name").getValue(String.class);

                        file_ref.child(academy_name).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                boolean check = false;
                                file_list.clear();
                                for (DataSnapshot fileSnapshot : snapshot.getChildren()) {
                                    String file_name = fileSnapshot.child("file_name").getValue(String.class);

                                    if (file_name != null) {
                                        check = true;

                                        Log.d("UploadActivity academy", "academy_name : " + academy_name);
                                        Log.d("UploadActivity TAG", "file_name : " + file_name);

                                        file_list.add(file_name);
                                    }

                                }

                                adapter = new DirectorFileListViewAdapter(UploadActivity.this, file_list, academy_name, new DirectorFileListViewAdapter.OnFileDeleteClickListener() {
                                    @Override
                                    public void onFileDelete(String fileName, String academy_name) {
                                        adapter.delete_File(fileName, academy_name);
                                        file_list.clear();
                                    }
                                });

                                adapter.notifyDataSetChanged();
                                listView.setAdapter(adapter);

                                if (!check) {
                                    empty_upload.setVisibility(View.VISIBLE);
                                } else {
                                    empty_upload.setVisibility(View.INVISIBLE);
                                }

                                Log.d("list array tag", "list array : " + file_list);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



    }

    //결과 처리
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //request코드가 0이고 OK를 선택했고 data에 뭔가가 들어 있다면
        if(requestCode == 0 && resultCode == RESULT_OK){
            filePath = data.getData();
            Log.d(TAG, "uri:" + String.valueOf(filePath));
            //Uri 파일을 Bitmap으로 만들어서 ImageView에 집어 넣는다.
//                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
//            Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(String.valueOf(filePath), MediaStore.Video.Thumbnails.MICRO_KIND);
//            Bitmap thumbnail = ThumbnailUtils.extractThumbnail(bitmap, 360, 480);
//            ivPreview.setImageBitmap(thumbnail);
        }

        if(requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            if(data != null && data.getData() != null) {
                imageUri = data.getData();
                img_profile.setImageURI(imageUri);

                showDialogUpload();
            }
        }
    }

    private void showDialogUpload() {
        final DatabaseReference normal_ref = FirebaseDatabase.getInstance().getReference(Common.MEMBER_INFO_REFERENCE);
        final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        AlertDialog.Builder builder = new AlertDialog.Builder(UploadActivity.this);
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
                    AlertDialog.Builder builder = new AlertDialog.Builder(UploadActivity.this);
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
                                    Intent intent = new Intent(UploadActivity.this, SplashScreenActivity.class);
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
                                    .setTextColor(ContextCompat.getColor(UploadActivity.this, android.R.color.holo_red_dark));
                            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                                    .setTextColor(ContextCompat.getColor(UploadActivity.this, R.color.colorAccent));
                        }
                    });
                    dialog.show();
                } else if (item.getItemId() == R.id.nav_upload) {
                    Intent intent = new Intent(UploadActivity.this, UploadActivity.class);
                    startActivity(intent);
                    finish();
                } else if (item.getItemId() == R.id.nav_academy_management) {
                    Intent intent = new Intent(UploadActivity.this, AcademyManagementActivity.class);
                    startActivity(intent);
                    finish();
                } else if (item.getItemId() == R.id.nav_director_home) {
                    Intent intent = new Intent(UploadActivity.this, DirectorHomeActivity.class);
                    startActivity(intent);
                    finish();
                } else if (item.getItemId() == R.id.nav_chatting_director) {
                    Intent intent = new Intent(UploadActivity.this, ChattingRoom_Director_Activity.class);
                    startActivity(intent);
                    finish();
                } else if (item.getItemId() == R.id.nav_downloader_management_director) {
                    Intent intent = new Intent(UploadActivity.this, Downloader_Management_Activity.class);
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


    //upload the file
    private void uploadFile(final String academy_name, final String upload_file_name) {

        FileDatabase = FirebaseDatabase.getInstance().getReference("FileList");
        mUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

//        final MemberInfoModel model = new MemberInfoModel();


        //업로드할 파일이 있으면 수행
        if (filePath != null && upload_file_name != null) {
            //업로드 진행 Dialog 보이기
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("업로드중...");
            progressDialog.show();

//            Log.d("filePath", "filePath" + filePath);

            //storage
            FirebaseStorage storage = FirebaseStorage.getInstance();

            //Unique한 파일명을 만들자.
//            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMHH_mmss");
//            Date now = new Date();
//            final String filename = formatter.format(now);
            //storage 주소와 폴더 파일명을 지정해 준다.
            StorageReference storageRef = storage.getReferenceFromUrl("gs://academyapp-d7c41.appspot.com").child("videos/" + upload_file_name);
            //올라가거라...
            storageRef.putFile(filePath)
                    //성공시
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            FileDatabase.child(academy_name).child(upload_file_name).child("file_name").setValue(upload_file_name);

                            progressDialog.dismiss(); //업로드 진행 Dialog 상자 닫기
                            Toast.makeText(getApplicationContext(), "업로드 완료!", Toast.LENGTH_SHORT).show();
                        }
                    })
                    //실패시
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(), "업로드 실패!", Toast.LENGTH_SHORT).show();
                        }
                    })
                    //진행중
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
//                            @SuppressWarnings("VisibleForTests") //이걸 넣어 줘야 아랫줄에 에러가 사라진다.
//                                    double progress = 100 * (taskSnapshot.getBytesTransferred() /  taskSnapshot.getTotalByteCount());
                            //dialog에 진행률을 퍼센트로 출력해 준다
                            progressDialog.setMessage("잠시만 기다려주세요.");
                        }
                    });
        } else if (filePath == null){
            Toast.makeText(getApplicationContext(), "파일을 선택하세요.", Toast.LENGTH_SHORT).show();
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
        NavController navController = Navigation.findNavController(this, R.id.nav_upload_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }


}