package com.example.academyapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.academyapp.Model.AcademyInfo;
import com.example.academyapp.RestAPI.GeocodingResponse;
import com.example.academyapp.RestAPI.RetrofitConnection;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.List;

import retrofit2.Call;

public class ModifyAcademyInfoActivity extends AppCompatActivity {

    private DatabaseReference academy_ref;
    private TextInputEditText modify_academy_name;
    private TextInputEditText modify_academy_address;
    private TextInputEditText modify_academy_tel;
    private TextInputEditText modify_academy_image_name;
    private Button modify_btn_image_choose;
    private Button btn_academy_modify;
    private TextInputEditText original_academy_image_name;

    private Uri academy_url;
    private Uri uri;


    private double ResultAddressX;
    private double ResultAddressY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_academy_info);

        modify_academy_name = (TextInputEditText) findViewById(R.id.modify_academy_name);
        modify_academy_address = (TextInputEditText) findViewById(R.id.modify_academy_address);
        modify_academy_tel = (TextInputEditText) findViewById(R.id.modify_academy_tel);
        modify_academy_image_name = (TextInputEditText) findViewById(R.id.modify_academy_image_name);
        modify_btn_image_choose = (Button) findViewById(R.id.modify_btn_image_choose);
        btn_academy_modify = (Button) findViewById(R.id.btn_academy_modify);
        original_academy_image_name = (TextInputEditText) findViewById(R.id.original_academy_image_name);

        academy_ref = FirebaseDatabase.getInstance().getReference(Common.ACADEMY_INFO_REFERENCE);
        academy_url = FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl();

        Intent intent = getIntent();
        final String academy = intent.getStringExtra("academy_name");

        academy_ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot info_snapshot : snapshot.getChildren()) {
                    String academy_name = info_snapshot.child("academy_name").getValue(String.class);

                    if (academy.equals(academy_name)) {
                        String academy_address = info_snapshot.child("academy_address").getValue(String.class);
                        String academy_image = info_snapshot.child("academy_image").getValue(String.class);
                        String academy_tel = info_snapshot.child("academy_tel").getValue(String.class);

                        modify_academy_name.setText(academy_name);
                        modify_academy_address.setText(academy_address);
                        modify_academy_tel.setText(academy_tel);
                        original_academy_image_name.setText(academy_image);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        modify_btn_image_choose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "이미지를 선택하세요."), 2000);
            }
        });


        btn_academy_modify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(modify_academy_name.getText().toString())) {
                    Toast.makeText(ModifyAcademyInfoActivity.this, "학원명을 입력하세요.", Toast.LENGTH_SHORT).show();
                    return;
                } else if (TextUtils.isEmpty(modify_academy_address.getText().toString())) {
                    Toast.makeText(ModifyAcademyInfoActivity.this, "주소를 입력하세요.", Toast.LENGTH_SHORT).show();
                    return;
                } else if (TextUtils.isEmpty(modify_academy_tel.getText().toString())) {
                    Toast.makeText(ModifyAcademyInfoActivity.this, "전화번호를 입력하세요.", Toast.LENGTH_SHORT).show();
                    return;
                } else if (TextUtils.isEmpty(modify_academy_image_name.getText().toString())) {
                    Toast.makeText(ModifyAcademyInfoActivity.this, "업로드 사진을 입력하세요.", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    final AcademyInfo model = new AcademyInfo();
                    model.setAcademy_name(modify_academy_name.getText().toString());
                    model.setAcademy_address(modify_academy_address.getText().toString());
                    model.setAcademy_tel(modify_academy_tel.getText().toString());
                    model.setAcademy_image(modify_academy_image_name.getText().toString());
                    model.setDirector_photo_url(String.valueOf(academy_url));

                    final String[] geocoding_string = new String[1];

                    RetrofitConnection retrofit_connection = new RetrofitConnection();
                    final Call<GeocodingResponse> call = retrofit_connection.mapAPI.getCoordinate(modify_academy_address.getText().toString());

                    Log.d("modify_academy_address", "modify_address : " + modify_academy_address.getText().toString());

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

                    final StorageReference academy_image_ref = FirebaseStorage.getInstance().getReferenceFromUrl("gs://academyapp-d7c41.appspot.com").child("academy_images/" + original_academy_image_name.getText().toString());

//                    upload_academy_image(modify_academy_image_name.getText().toString());

                    if (uri != null && modify_academy_image_name.getText().toString() != null) {
                        //업로드 진행 Dialog 보이기
                        final ProgressDialog progressDialog = new ProgressDialog(ModifyAcademyInfoActivity.this);
                        progressDialog.setTitle("업로드중...");
                        progressDialog.show();


                        FirebaseStorage storage = FirebaseStorage.getInstance();
                        final StorageReference storageRef = storage.getReferenceFromUrl("gs://academyapp-d7c41.appspot.com").child("academy_images/" + modify_academy_image_name.getText().toString());

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

                                        storageRef.putFile(uri)
                                                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                                    @Override
                                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                                        progressDialog.dismiss(); //업로드 진행 Dialog 상자 닫기

                                                        academy_ref.child(modify_academy_name.getText().toString()).setValue(model)
                                                                .addOnFailureListener(new OnFailureListener() {
                                                                    @Override
                                                                    public void onFailure(@NonNull Exception e) {
                                                                        Toast.makeText(ModifyAcademyInfoActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                                                    }
                                                                })
                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void aVoid) {
                                                                        academy_image_ref.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                            @Override
                                                                            public void onSuccess(Void aVoid) {
                                                                                Toast.makeText(ModifyAcademyInfoActivity.this, "학원 정보가 수정되었습니다.", Toast.LENGTH_SHORT).show();
                                                                                Intent intent = new Intent(ModifyAcademyInfoActivity.this, AcademyManagementActivity.class);
                                                                                startActivity(intent);
                                                                                finish();
                                                                            }
                                                                        }).addOnFailureListener(new OnFailureListener() {
                                                                            @Override
                                                                            public void onFailure(@NonNull Exception e) {
                                                                                Toast.makeText(getApplicationContext(), e.getMessage() + "으로 인한 기존 이미지 파일 삭제 실패", Toast.LENGTH_SHORT).show();
                                                                            }
                                                                        });
                                                                    }
                                                                });
                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                progressDialog.dismiss();
                                                Toast.makeText(getApplicationContext(), "업로드 실패!", Toast.LENGTH_SHORT).show();
                                            }
                                        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                                            @Override
                                            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                                progressDialog.setMessage("잠시만 기다려주세요.");
                                            }
                                        });
                                    }
                                });
                    } else if (uri == null){
                        Toast.makeText(getApplicationContext(), "파일을 선택하세요.", Toast.LENGTH_SHORT).show();
                    }


//                    academy_ref.child(modify_academy_name.getText().toString()).setValue(model)
//                            .addOnFailureListener(new OnFailureListener() {
//                                @Override
//                                public void onFailure(@NonNull Exception e) {
//                                    Toast.makeText(ModifyAcademyInfoActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
//                                }
//                            })
//                            .addOnSuccessListener(new OnSuccessListener<Void>() {
//                                @Override
//                                public void onSuccess(Void aVoid) {
//                                    academy_image_ref.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
//                                        @Override
//                                        public void onSuccess(Void aVoid) {
//                                            Toast.makeText(ModifyAcademyInfoActivity.this, "학원 정보가 수정되었습니다.", Toast.LENGTH_SHORT).show();
//                                            finish();
//                                        }
//                                    }).addOnFailureListener(new OnFailureListener() {
//                                        @Override
//                                        public void onFailure(@NonNull Exception e) {
//                                            Toast.makeText(getApplicationContext(), e.getMessage() + "으로 인한 기존 이미지 파일 삭제 실패", Toast.LENGTH_SHORT).show();
//                                        }
//                                    });
//                                }
//                            });

                }
            }
        });



    }


//    private void upload_academy_image(final String upload_file_name) {
//
////        업로드할 파일이 있으면 수행
//        if (uri != null && upload_file_name != null) {
//            //업로드 진행 Dialog 보이기
//            final ProgressDialog progressDialog = new ProgressDialog(this);
//            progressDialog.setTitle("업로드중...");
//            progressDialog.show();
//
//
//            //storage
//            FirebaseStorage storage = FirebaseStorage.getInstance();
//
//            //storage 주소와 폴더 파일명을 지정해 준다.
//            StorageReference storageRef = storage.getReferenceFromUrl("gs://academyapp-d7c41.appspot.com").child("academy_images/" + upload_file_name);
//            //올라가거라...
//            storageRef.putFile(uri)
//                    //성공시
//                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//                        @Override
//                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//
//
//                            progressDialog.dismiss(); //업로드 진행 Dialog 상자 닫기
//                            Toast.makeText(getApplicationContext(), "업로드 완료!", Toast.LENGTH_SHORT).show();
//                        }
//                    })
//                    //실패시
//                    .addOnFailureListener(new OnFailureListener() {
//                        @Override
//                        public void onFailure(@NonNull Exception e) {
//                            progressDialog.dismiss();
//                            Toast.makeText(getApplicationContext(), "업로드 실패!", Toast.LENGTH_SHORT).show();
//                        }
//                    })
//                    //진행중
//                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
//                        @Override
//                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
////                            @SuppressWarnings("VisibleForTests") //이걸 넣어 줘야 아랫줄에 에러가 사라진다.
////                                    double progress = 100 * (taskSnapshot.getBytesTransferred() /  taskSnapshot.getTotalByteCount());
//                            //dialog에 진행률을 퍼센트로 출력해 준다
//                            progressDialog.setMessage("잠시만 기다려주세요.");
//                        }
//                    });
//        } else if (uri == null){
//            Toast.makeText(getApplicationContext(), "파일을 선택하세요.", Toast.LENGTH_SHORT).show();
//        }
//    }
//


    private String getName(Uri uri) {
        String[] projection = {MediaStore.Images.ImageColumns.DISPLAY_NAME};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:{
                finish();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 2000 && resultCode == RESULT_OK) {
            uri = data.getData();
            String file_name_confirm = getName(uri);
            Log.d("file_string", "file_string : " + file_name_confirm);
            Log.d("uri", "uri : " + String.valueOf(uri));

//            try {
//                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
//                preview_image.setImageBitmap(bitmap);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
            modify_academy_image_name.setText(file_name_confirm);
        }
    }
}