package com.example.academyapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
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
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.academyapp.Model.AcademyInfo;
import com.example.academyapp.RestAPI.GeocodingResponse;
import com.example.academyapp.RestAPI.RetrofitConnection;
import com.example.academyapp.Utils.UserUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraAnimation;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.overlay.InfoWindow;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.Overlay;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AcademyManagementActivity extends AppCompatActivity implements OnMapReadyCallback {

    DatabaseReference AcademyInfoRef;

    private DrawerLayout drawer;
    private NavigationView navigationView;
    private AppBarConfiguration mAppBarConfiguration;
    private NavController navController;

    private AlertDialog waitingDialog;
    private StorageReference storageReference;
    private ImageView img_profile;
    private Uri imageUri;
    private double ResultAddressX;
    private double ResultAddressY;
    private Uri uri;
    private TextInputEditText academy_image_name;

    private static final int PICK_IMAGE_REQUEST = 100;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_academy_management);

        Toolbar toolbar = findViewById(R.id.toolbar_management);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("학원 정보 관리");

        drawer = findViewById(R.id.drawer_director_academy_management);

        navigationView = findViewById(R.id.nav_management_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_director_home, R.id.nav_director_logout, R.id.nav_upload, R.id.nav_academy_management, R.id.nav_chatting_director, R.id.nav_downloader_management_director)
                .setDrawerLayout(drawer)
                .build();
        navController = Navigation.findNavController(this, R.id.nav_management_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        AcademyInfoRef = FirebaseDatabase.getInstance().getReference(Common.ACADEMY_INFO_REFERENCE);

        confirmAcademyInfo();

        MapFragment mapFragment = (MapFragment) getSupportFragmentManager().findFragmentById(R.id.map_management);
        if (mapFragment == null) {
            mapFragment = MapFragment.newInstance();
            getSupportFragmentManager().beginTransaction().add(R.id.map_management, mapFragment).commit();
        }

        mapFragment.getMapAsync(this);

        init();
    }

    @UiThread
    @Override
    public void onMapReady(@NonNull final NaverMap naverMap) {

//        if (ResultAddressX != 0 && ResultAddressY != 0) {

//        final Marker marker = new Marker();
//        final InfoWindow infoWindow = new InfoWindow();
//
//
//        marker.setMap(null);
//        infoWindow.setMap(null);

            Log.d("logic check", "check confirm");

            AcademyInfoRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    Uri profile_url = FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl();

                    for (DataSnapshot checkSnapshot : snapshot.getChildren()) {

                        String photo_url_string = checkSnapshot.child("director_photo_url").getValue(String.class);

                        if (photo_url_string != null) {

                            if (String.valueOf(profile_url).equals(photo_url_string)) {
                                String location = checkSnapshot.child("academy_address").getValue(String.class);
                                final String academy_name = checkSnapshot.child("academy_name").getValue(String.class);

                                RetrofitConnection retrofitConnection = new RetrofitConnection();
                                Call<GeocodingResponse> geocodingResponse = retrofitConnection.mapAPI.getCoordinate(location);

                                Log.d("location", "location : " + location);

                                Log.d("director_url tag", "director_url : " + photo_url_string + ", current_user_url : " + String.valueOf(profile_url));

                                geocodingResponse.enqueue(new Callback<GeocodingResponse>() {
                                    @Override
                                    public void onResponse(Call<GeocodingResponse> call, Response<GeocodingResponse> response) {
                                        if (response.isSuccessful()) {
                                            GeocodingResponse geocodingResponse = response.body();
                                            List<GeocodingResponse.RequestAddress> addressList = geocodingResponse.getAddresses();

                                            ResultAddressX = addressList.get(0).getX();
                                            ResultAddressY = addressList.get(0).getY();

                                            Log.d("marker_x", "marker_x : " + ResultAddressX);
                                            Log.d("marker_y", "marker_y : " + ResultAddressY);

                                            CameraUpdate cameraUpdate = CameraUpdate.scrollTo(new LatLng(ResultAddressY, ResultAddressX)).animate(CameraAnimation.Fly);
                                            naverMap.moveCamera(cameraUpdate);

                                            Marker marker = new Marker();
                                            marker.setPosition(new LatLng(ResultAddressY, ResultAddressX));
                                            marker.setMap(naverMap);

                                            final InfoWindow infoWindow = new InfoWindow();
                                            infoWindow.setAdapter(new InfoWindow.DefaultTextAdapter(AcademyManagementActivity.this) {
                                                @NonNull
                                                @Override
                                                public CharSequence getText(@NonNull InfoWindow infoWindow) {
                                                    return academy_name;
                                                }
                                            });
                                            infoWindow.open(marker);

                                            infoWindow.setOnClickListener(new Overlay.OnClickListener() {
                                                @Override
                                                public boolean onClick(@NonNull Overlay overlay) {
                                                    Intent intent = new Intent(getApplicationContext(), ModifyAcademyInfoActivity.class);
                                                    intent.putExtra("academy_name", academy_name);
                                                    startActivity(intent);
                                                    return true;
                                                }
                                            });
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<GeocodingResponse> call, Throwable t) {
                                        Log.d("ERROR", "Failure Log :" + t.toString());
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

//        }

    }

//    @Override
//    protected void onResume() {
//        super.onResume();
//    }

    private void confirmAcademyInfo() {

        final Uri photo_url = FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl();

        AcademyInfoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                boolean isRegistered = false;

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String academy_photo_url = dataSnapshot.child("director_photo_url").getValue(String.class);

                    Log.d("url comparison", "data url : " + academy_photo_url + ", profile url : " + String.valueOf(photo_url));

                    if (String.valueOf(photo_url).equals(academy_photo_url)) {
                        isRegistered = true;
                        break;
                    }

                }
                Log.d("isRegistered tag", "isRegistered result: " + isRegistered);

                if (isRegistered == false) {
                    showRegisterAcademy();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void showRegisterAcademy() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View itemView = LayoutInflater.from(this).inflate(R.layout.academy_register, null);

        final TextInputEditText academy_name = (TextInputEditText) itemView.findViewById(R.id.edt_academy_name);
        final TextInputEditText academy_address = (TextInputEditText) itemView.findViewById(R.id.edt_academy_address);
        final TextInputEditText academy_tel = (TextInputEditText) itemView.findViewById(R.id.edt_academy_tel);
        academy_image_name = (TextInputEditText) itemView.findViewById(R.id.academy_image_name);

        Button btn_academy_register = (Button) itemView.findViewById(R.id.btn_academy_register);
        Button btn_academy_image_choose = (Button) itemView.findViewById(R.id.btn_image_choose);

        builder.setView(itemView);
        final AlertDialog dialog = builder.create();
        dialog.show();

        btn_academy_image_choose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "이미지를 선택하세요."), 2000);
            }
        });

        FirebaseUser director_ref = FirebaseAuth.getInstance().getCurrentUser();
        final String director_photo_url = director_ref.getPhotoUrl().toString();

        btn_academy_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(academy_name.getText().toString())) {
                    Toast.makeText(AcademyManagementActivity.this, "학원명을 입력하세요.", Toast.LENGTH_SHORT).show();
                    return;
                } else if (TextUtils.isEmpty(academy_address.getText().toString())) {
                    Toast.makeText(AcademyManagementActivity.this, "주소를 입력하세요.", Toast.LENGTH_SHORT).show();
                    return;
                } else if (TextUtils.isEmpty(academy_tel.getText().toString())) {
                    Toast.makeText(AcademyManagementActivity.this, "전화번호를 입력하세요.", Toast.LENGTH_SHORT).show();
                    return;
                } else if (TextUtils.isEmpty(academy_image_name.getText().toString())) {
                    Toast.makeText(AcademyManagementActivity.this, "업로드 사진을 입력하세요.", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    final AcademyInfo model = new AcademyInfo();
                    model.setAcademy_name(academy_name.getText().toString());
                    model.setAcademy_address(academy_address.getText().toString());
                    model.setAcademy_tel(academy_tel.getText().toString());
                    model.setAcademy_image(academy_image_name.getText().toString());
                    model.setDirector_photo_url(director_photo_url);

                    final String[] geocoding_string = new String[1];

                    RetrofitConnection retrofit_connection = new RetrofitConnection();
                    final Call<GeocodingResponse> call = retrofit_connection.mapAPI.getCoordinate(academy_address.getText().toString());

                    Log.d("academy_address", "address : " + academy_address.getText().toString());

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                               GeocodingResponse geocoding = call.execute().body();
                                List<GeocodingResponse.RequestAddress> geocodeList = geocoding.getAddresses();

                                ResultAddressX = geocodeList.get(0).getX();
                                ResultAddressY = geocodeList.get(0).getY();

                                Log.d("address_x", "address_x : " + ResultAddressX);
                                Log.d("address_y", "address_y : " + ResultAddressY);

                                model.setX(ResultAddressX);
                                model.setY(ResultAddressY);

                                geocoding_string[0] = ResultAddressX + ", " + ResultAddressY;

                                Log.d("geocoding_string1", "geocoding_string1 : " + geocoding_string[0]);


                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();

                    try {
                        Thread.sleep(500);
                    } catch(Exception e) {
                        e.printStackTrace();
                    }

                    Log.d("geocoding_string2", "geocoding_string2 : " + geocoding_string[0]);

                    FirebaseMessaging.getInstance().getToken()
                            .addOnCompleteListener(new OnCompleteListener<String>() {
                                @Override
                                public void onComplete(@NonNull Task<String> task) {
                                    if (!task.isSuccessful()) {
                                        Log.d("getToken fail tag", "getToken failed", task.getException());
                                        return;
                                    }

                                    // Get new Instance ID token
                                    String token = task.getResult();
                                    Log.d("token", "token string : " + token);
                                    model.setToken(token);

                                    upload_academy_image(academy_image_name.getText().toString());

                                    AcademyInfoRef.child(academy_name.getText().toString()).setValue(model)
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    dialog.dismiss();
                                                    Toast.makeText(AcademyManagementActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            })
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Toast.makeText(AcademyManagementActivity.this, "학원 정보 등록이 완료되었습니다.", Toast.LENGTH_SHORT).show();
                                                    dialog.dismiss();
                                                }
                                            });
                                }
                            });
                }
            }
        });

    }

    private void upload_academy_image(final String upload_file_name) {

        //업로드할 파일이 있으면 수행
        if (uri != null && upload_file_name != null) {
            //업로드 진행 Dialog 보이기
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("업로드중...");
            progressDialog.show();


            //storage
            FirebaseStorage storage = FirebaseStorage.getInstance();

            //storage 주소와 폴더 파일명을 지정해 준다.
            StorageReference storageRef = storage.getReferenceFromUrl("gs://academyapp-d7c41.appspot.com").child("academy_images/" + upload_file_name);
            //올라가거라...
            storageRef.putFile(uri)
                    //성공시
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

//                            FileDatabase.child(academy_name).child("file_name").setValue(upload_file_name);

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
        } else if (uri == null){
            Toast.makeText(getApplicationContext(), "파일을 선택하세요.", Toast.LENGTH_SHORT).show();
        }
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
        } else if (requestCode == 2000 && resultCode == RESULT_OK) {
            uri = data.getData();
            String file_name_confirm = getName(uri);
            Log.d("file_string", "file_string : " + file_name_confirm);
            Log.d("uri", "uri : " + String.valueOf(uri));

            academy_image_name.setText(file_name_confirm);
        }
    }

    private String getName(Uri uri) {
        String[] projection = {MediaStore.Images.ImageColumns.DISPLAY_NAME};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

//    private String getUri(Uri uri_content) {
//        String id = DocumentsContract.getDocumentId(uri).split(":")[1];
//        String[] columns = {MediaStore.Files.FileColumns.DATA};
//        String select = MediaStore.Images.Media._ID + "=?";
//        Cursor cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, select, new String[]{id}, null);
//
//        String file_path = "";
//        int columnIndex = cursor.getColumnIndex(columns[0]);
//
//        if (cursor.moveToFirst()) {
//            file_path = cursor.getString(columnIndex);
//        }
//
//        cursor.close();
//
//        return file_path;
//    }

    private void showDialogUpload() {
        AlertDialog.Builder builder = new AlertDialog.Builder(AcademyManagementActivity.this);
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
                    AlertDialog.Builder builder = new AlertDialog.Builder(AcademyManagementActivity.this);
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
                                    Intent intent = new Intent(AcademyManagementActivity.this, SplashScreenActivity.class);
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
                                    .setTextColor(ContextCompat.getColor(AcademyManagementActivity.this, android.R.color.holo_red_dark));
                            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                                    .setTextColor(ContextCompat.getColor(AcademyManagementActivity.this, R.color.colorAccent));
                        }
                    });
                    dialog.show();
                } else if (item.getItemId() == R.id.nav_upload) {
                    Intent intent = new Intent(AcademyManagementActivity.this, UploadActivity.class);
                    startActivity(intent);
                    finish();
                } else if (item.getItemId() == R.id.nav_academy_management) {
                    Intent intent = new Intent(AcademyManagementActivity.this, AcademyManagementActivity.class);
                    startActivity(intent);
                    finish();
                } else if (item.getItemId() == R.id.nav_director_home) {
                    Intent intent = new Intent(AcademyManagementActivity.this, DirectorHomeActivity.class);
                    startActivity(intent);
                    finish();
                }  else if (item.getItemId() == R.id.nav_chatting_director) {
                    Intent intent = new Intent(AcademyManagementActivity.this, ChattingRoom_Director_Activity.class);
                    startActivity(intent);
                    finish();
                } else if (item.getItemId() == R.id.nav_downloader_management_director) {
                    Intent intent = new Intent(AcademyManagementActivity.this, Downloader_Management_Activity.class);
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



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.customer_home, menu);
        return true;
    }


    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_management_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }


}