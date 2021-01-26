package com.example.academyapp;

import androidx.annotation.NonNull;
import androidx.annotation.UiThread;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.academyapp.Model.MemberInfoModel;
import com.example.academyapp.RestAPI.RequestAddress;
import com.example.academyapp.RestAPI.RetrofitConnection;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AcademyManagementActivity extends AppCompatActivity implements OnMapReadyCallback {

    DatabaseReference UserInfoRef;
    String address;
    double ResultAddressX;
    double ResultAddressY;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_academy_management);

        confirmAcademyInfo();
    }

    @UiThread
    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {
        CameraUpdate cameraUpdate = CameraUpdate.scrollTo(new LatLng(ResultAddressX, ResultAddressY));
        naverMap.moveCamera(cameraUpdate);
    }

    private void confirmAcademyInfo() {

        UserInfoRef = FirebaseDatabase.getInstance().getReference(Common.MEMBER_INFO_REFERENCE);
        String mUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        UserInfoRef.child(mUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String location = snapshot.child("location").getValue(String.class);

                if (location == null) {
                    showRegisterAcademy();
                } else {
                    showAcademyManagement();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void showAcademyManagement() {

        RetrofitConnection retrofitConnection = new RetrofitConnection();
        Call<RequestAddress> requestAddress = retrofitConnection.mapAPI.getCoordinate(address);

        requestAddress.enqueue(new Callback<RequestAddress>() {
            @Override
            public void onResponse(Call<RequestAddress> call, Response<RequestAddress> response) {
                if (response.isSuccessful()) {
                    ResultAddressX = response.body().getX();
                    ResultAddressY = response.body().getY();
                }
            }

            @Override
            public void onFailure(Call<RequestAddress> call, Throwable t) {
                Log.d("ERROR", "Failure Log :" + t.toString());
            }
        });

        LatLng coord = new LatLng(ResultAddressX, ResultAddressY);

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
                    final MemberInfoModel model = new MemberInfoModel();
                    model.setAcademy_name(academy_name.getText().toString());
                    model.setAcademy_address(academy_address.getText().toString());
                    model.setAcademy_tel(academy_tel.getText().toString());

                    UserInfoRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
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
}