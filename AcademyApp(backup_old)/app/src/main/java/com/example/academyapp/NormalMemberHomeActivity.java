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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.academyapp.RestAPI.GeocodingResponse;
import com.example.academyapp.RestAPI.RetrofitConnection;
import com.example.academyapp.Utils.UserUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraAnimation;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.NaverMapSdk;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.overlay.InfoWindow;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.Overlay;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NormalMemberHomeActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int PICK_IMAGE_REQUEST = 101;

    private AppBarConfiguration mAppBarConfiguration;
    private DrawerLayout drawer;
    private NavigationView navigationView;
    private NavController navController;

    private AlertDialog waitingDialog;
    private StorageReference storageReference;
    private ImageView img_profile;
    private Uri imageUri;
    private double addressX;
    private double addressY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_normal_member_home);

        Toolbar toolbar = findViewById(R.id.toolbar2);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab2);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        drawer = findViewById(R.id.drawer_normalmember_layout);
        navigationView = findViewById(R.id.nav_normalmember_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_normalmember_home, R.id.nav_normalmember_logout, R.id.nav_download, R.id.nav_chatting)
                .setDrawerLayout(drawer)
                .build();
        navController = Navigation.findNavController(this, R.id.nav_normalmember_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);


//        NaverMapSdk.getInstance(this).setClient(
//                new NaverMapSdk.NaverCloudPlatformClient("z79q0dob9r"));

        FragmentManager fm = getSupportFragmentManager();
        MapFragment mapFragment = (MapFragment) fm.findFragmentById(R.id.map2);
        if (mapFragment == null) {
            mapFragment = MapFragment.newInstance();
            fm.beginTransaction().add(R.id.map2, mapFragment).commit();
        }

        mapFragment.getMapAsync(this);

        init();

    }

    @UiThread
    @Override
    public void onMapReady(@NonNull final NaverMap naverMap) {

        DatabaseReference academy_ref = FirebaseDatabase.getInstance().getReference(Common.ACADEMY_INFO_REFERENCE);

        academy_ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {

                    String address = dataSnapshot.child("academy_address").getValue(String.class);
                    final String name = dataSnapshot.child("academy_name").getValue(String.class);

                    RetrofitConnection retrofitConnection = new RetrofitConnection();
                    Call<GeocodingResponse> geocodingResponse = retrofitConnection.mapAPI.getCoordinate(address);

                    geocodingResponse.enqueue(new Callback<GeocodingResponse>() {
                        @Override
                        public void onResponse(Call<GeocodingResponse> call, Response<GeocodingResponse> response) {
                            GeocodingResponse geocodingResponse = response.body();
                            List<GeocodingResponse.RequestAddress> addressList = geocodingResponse.getAddresses();

                            addressX = addressList.get(0).getX();
                            addressY = addressList.get(0).getY();

                            Log.d("marker_x", "marker_x : " + addressX);
                            Log.d("marker_y", "marker_y : " + addressY);

//                            CameraUpdate cameraUpdate = CameraUpdate.scrollTo(new LatLng(addressY, addressX)).animate(CameraAnimation.Fly);
//                            naverMap.moveCamera(cameraUpdate);

                            final Marker marker = new Marker();
                            marker.setPosition(new LatLng(addressY, addressX));
                            marker.setMap(naverMap);

                            marker.setOnClickListener(new Overlay.OnClickListener() {
                                @Override
                                public boolean onClick(@NonNull Overlay overlay) {

                                    final InfoWindow infoWindow = new InfoWindow();
                                    infoWindow.setAdapter(new InfoWindow.DefaultTextAdapter(NormalMemberHomeActivity.this) {
                                        @NonNull
                                        @Override
                                        public CharSequence getText(@NonNull InfoWindow infoWindow) {
                                            return name;
                                        }
                                    });
                                    infoWindow.open(marker);

                                    infoWindow.setOnClickListener(new Overlay.OnClickListener() {
                                        @Override
                                        public boolean onClick(@NonNull Overlay overlay) {
//                                            Toast.makeText(getApplicationContext(), "마커 클릭 확인", Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(NormalMemberHomeActivity.this, AcademyInfoActivity.class);
                                            intent.putExtra("academy_name", name);
                                            startActivity(intent);
                                            infoWindow.close();
                                            return true;
                                        }
                                    });

                                    return false;
                                }
                            });





                        }

                        @Override
                        public void onFailure(Call<GeocodingResponse> call, Throwable t) {

                        }
                    });

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }


    private void showDialogUpload() {
        AlertDialog.Builder builder = new AlertDialog.Builder(NormalMemberHomeActivity.this);
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
                if (item.getItemId() == R.id.nav_normalmember_logout) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(NormalMemberHomeActivity.this);
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
                                    Intent intent = new Intent(NormalMemberHomeActivity.this, SplashScreenActivity.class);
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
                                    .setTextColor(ContextCompat.getColor(NormalMemberHomeActivity.this, android.R.color.holo_red_dark));
                            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                                    .setTextColor(ContextCompat.getColor(NormalMemberHomeActivity.this, R.color.colorAccent));
                        }
                    });
                    dialog.show();
                } else if (item.getItemId() == R.id.nav_download) {
                    Intent intent = new Intent(NormalMemberHomeActivity.this, DownloadContentsActivity.class);
                    startActivity(intent);
                    finish();
                } else if (item.getItemId() == R.id.nav_normalmember_home) {
                    Intent intent = new Intent(NormalMemberHomeActivity.this, NormalMemberHomeActivity.class);
                    startActivity(intent);
                    finish();
                } else if (item.getItemId() == R.id.nav_chatting) {
                    Intent intent = new Intent(NormalMemberHomeActivity.this, ChattingRoom_Normal_Activity.class);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.customer_home, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_normalmember_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}