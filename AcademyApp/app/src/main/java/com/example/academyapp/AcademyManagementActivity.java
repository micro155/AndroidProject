package com.example.academyapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
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
import com.example.academyapp.Model.MemberInfoModel;
import com.example.academyapp.RestAPI.RequestAddress;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
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
import com.naver.maps.map.overlay.Marker;

import org.json.simple.JSONArray;
import org.json.JSONException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AcademyManagementActivity extends AppCompatActivity implements OnMapReadyCallback {

    DatabaseReference AcademyInfoRef;
    String ResultAddressX;
    String ResultAddressY;
    String mUid;

    private DrawerLayout drawer;
    private NavigationView navigationView;
    private AppBarConfiguration mAppBarConfiguration;
    private NavController navController;

    private AlertDialog waitingDialog;
    private StorageReference storageReference;
    private ImageView img_profile;
    private Uri imageUri;

    private static final int PICK_IMAGE_REQUEST = 100;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_academy_management);

        confirmAcademyInfo();
        showAcademyManagement();

        Toolbar toolbar = findViewById(R.id.toolbar_management);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_director_academy_management);

        navigationView = findViewById(R.id.nav_management_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_director_home, R.id.nav_director_logout, R.id.nav_upload, R.id.nav_academy_management)
                .setDrawerLayout(drawer)
                .build();
        navController = Navigation.findNavController(this, R.id.nav_management_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);


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
    public void onMapReady(@NonNull NaverMap naverMap) {
        CameraUpdate cameraUpdate = CameraUpdate.scrollTo(new LatLng(36.763695, 127.281796)).animate(CameraAnimation.Fly);
        naverMap.moveCamera(cameraUpdate);

        Marker marker = new Marker();
        marker.setPosition(new LatLng(36.763695, 127.281796));
        marker.setMap(naverMap);
    }

    private void confirmAcademyInfo() {

        AcademyInfoRef = FirebaseDatabase.getInstance().getReference(Common.ACADEMY_INFO_REFERENCE);
        mUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        AcademyInfoRef.child(mUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String location = snapshot.child("academy_address").getValue(String.class);

                if (location == null) {
                    showRegisterAcademy();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void showAcademyManagement() {

//        final String[] academy_addr = new String[1];

        AcademyInfoRef.child(mUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String location = snapshot.child("academy_address").getValue(String.class);

                RetrofitConnection retrofitConnection = new RetrofitConnection();
                Call<RequestAddress> requestAddress = retrofitConnection.mapAPI.getCoordinate(location);
                Log.d("location", location);

                requestAddress.enqueue(new Callback<RequestAddress>() {
                    @Override
                    public void onResponse(Call<RequestAddress> call, Response<RequestAddress> response) {
                        if (response.isSuccessful()) {
                            ResultAddressX = response.body().getX();
                            ResultAddressY = response.body().getY();

                            Log.d("x", "x = " + ResultAddressX);
                            Log.d("y", "y = " + ResultAddressY);
                        }
                    }

                    @Override
                    public void onFailure(Call<RequestAddress> call, Throwable t) {
                        Log.d("ERROR", "Failure Log :" + t.toString());
                    }
                });


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

//        Log.d("x out", "x : " + ResultAddressX);
//        Log.d("y out", "y : " + ResultAddressY);
//        LatLng coord = new LatLng(ResultAddressX, ResultAddressY);

    }


    private void showRegisterAcademy() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View itemView = LayoutInflater.from(this).inflate(R.layout.academy_register, null);

        final TextInputEditText academy_name = (TextInputEditText)itemView.findViewById(R.id.edt_academy_name);
        final TextInputEditText academy_address = (TextInputEditText)itemView.findViewById(R.id.edt_academy_address);
        final TextInputEditText academy_tel = (TextInputEditText)itemView.findViewById(R.id.edt_academy_tel);

        Button btn_academy_register = (Button)itemView.findViewById(R.id.btn_academy_register);

        builder.setView(itemView);
        final AlertDialog dialog = builder.create();
        dialog.show();

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
                } else {
                    final AcademyInfo model = new AcademyInfo();
                    model.setAcademy_name(academy_name.getText().toString());
                    model.setAcademy_address(academy_address.getText().toString());
                    model.setAcademy_tel(academy_tel.getText().toString());

                    AcademyInfoRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .setValue(model)
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
            }
        });

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