package com.example.academyapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.academyapp.Model.MemberInfoModel;
import com.firebase.ui.auth.AuthMethodPickerLayout;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;


import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Action;

public class SplashScreenActivity extends AppCompatActivity {

    ProgressBar progressBar;

    private final static int LOGIN_REQUEST_CODE = 777;
    private List<AuthUI.IdpConfig> providers;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener listener;
    private String mUid;

    FirebaseDatabase database;
    DatabaseReference UserInfoRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        init();

    }

    @Override
    protected void onStart() {
        super.onStart();
        delaySplashScreen();
    }

    @Override
    protected void onStop() {
        if(firebaseAuth != null && listener != null) {
            firebaseAuth.removeAuthStateListener(listener);
        }
        super.onStop();
    }

    private void init() {

        database = FirebaseDatabase.getInstance();
        UserInfoRef = database.getReference(Common.MEMBER_INFO_REFERENCE);

        providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build());

        firebaseAuth = FirebaseAuth.getInstance();
        listener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    checkUserFromFirebase();
                } else {
                    showLoginLayout();
                }
            }
        };

    }

    private void checkUserFromFirebase() {

        mUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        UserInfoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String uid = dataSnapshot.child("uid").getValue(String.class);

                    Log.d("uid", "uid : " + uid);
                    Log.d("mUid", "mUid : " + mUid);

                    if (uid != null) {
                        if (uid.equals(mUid)) {
                            MemberInfoModel memberInfoModel = snapshot.getValue(MemberInfoModel.class);
                            goToHomeActivity(memberInfoModel);
                            finish();
                        }
                    }
                }
                showRegisterLayout();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SplashScreenActivity.this, "" + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showRegisterLayout() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View itemView = LayoutInflater.from(this).inflate(R.layout.layout_register, null);
        final String Uid = firebaseAuth.getCurrentUser().getUid();

        final RadioGroup rg = itemView.findViewById(R.id.radioGroup);
        final RadioButton normalmem = itemView.findViewById(R.id.normalmember);
        final RadioButton directormem = itemView.findViewById(R.id.directormember);

        final TextInputEditText txt_name = (TextInputEditText)itemView.findViewById(R.id.edt_name);
        final TextInputEditText txt_phone = (TextInputEditText)itemView.findViewById(R.id.edt_phone_number);
        final TextInputEditText txt_nickname = (TextInputEditText)itemView.findViewById(R.id.edt_nick_name);

        Button btn_continue = (Button)itemView.findViewById(R.id.btn_register);

        if (FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber() != null && !TextUtils.isEmpty(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber())) {
            txt_phone.setText(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber());
        }

        builder.setView(itemView);
        final AlertDialog dialog = builder.create();
        dialog.show();

        btn_continue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(txt_name.getText().toString())) {
                    Toast.makeText(SplashScreenActivity.this, "이름을 입력하세요.", Toast.LENGTH_SHORT).show();
                    return;
                } else if (TextUtils.isEmpty(txt_phone.getText().toString())) {
                    Toast.makeText(SplashScreenActivity.this, "연락처를 입력하세요.", Toast.LENGTH_SHORT).show();
                    return;
                } else if (TextUtils.isEmpty(txt_nickname.getText().toString())) {
                    Toast.makeText(SplashScreenActivity.this, "닉네임을 입력하세요.", Toast.LENGTH_SHORT).show();
                    return;
                } else if (rg.getCheckedRadioButtonId() == -1){
                    Toast.makeText(SplashScreenActivity.this, "회원 분류를 선택하세요.", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    final MemberInfoModel model = new MemberInfoModel();
                    model.setName(txt_name.getText().toString());
                    model.setPhoneNumber(txt_phone.getText().toString());
                    model.setNickName(txt_nickname.getText().toString());
                    model.setUid(Uid);

                    if (normalmem.isChecked()) {
                        model.setType(normalmem.getText().toString());
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

                                        UserInfoRef.child(txt_nickname.getText().toString())
                                                .setValue(model)
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        dialog.dismiss();
                                                        Toast.makeText(SplashScreenActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                                    }
                                                })
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        Toast.makeText(SplashScreenActivity.this, "회원가입이 완료되었습니다.", Toast.LENGTH_SHORT).show();
                                                        dialog.dismiss();
                                                        goToHomeActivity(model);
                                                    }
                                                });
                                    }
                                });

                    } else if (directormem.isChecked()) {
                        model.setType(directormem.getText().toString());
                        UserInfoRef.child(txt_nickname.getText().toString())
                                .setValue(model)
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        dialog.dismiss();
                                        Toast.makeText(SplashScreenActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(SplashScreenActivity.this, "회원가입이 완료되었습니다.", Toast.LENGTH_SHORT).show();
                                        dialog.dismiss();
                                        goToHomeActivity(model);
                                    }
                                });
                    }
                }
            }
        });
    }

    private void goToHomeActivity(MemberInfoModel memberInfoModel) {

        mUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Common.currentMember = memberInfoModel;

        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference(Common.MEMBER_INFO_REFERENCE);

        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String uid = dataSnapshot.child("uid").getValue(String.class);

                    if (uid != null) {
                        if (uid.equals(mUid)) {
                            String type = dataSnapshot.child("type").getValue(String.class);
                            if (type.equals("일반회원")) {
                                startActivity(new Intent(SplashScreenActivity.this, NormalMemberHomeActivity.class));
                                Log.d("route", "normalMember route");
                            } else {
                                startActivity(new Intent(SplashScreenActivity.this, DirectorHomeActivity.class));
                                Log.d("route", "director route");
                            }
                        }
                    }
                }
                finish();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void showLoginLayout() {
        AuthMethodPickerLayout authMethodPickerLayout = new AuthMethodPickerLayout
                .Builder(R.layout.layout_sign_in)
                .setEmailButtonId(R.id.btn_email_sign_in)
                .setGoogleButtonId(R.id.btn_google_sign_in)
                .build();

        startActivityForResult(AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAuthMethodPickerLayout(authMethodPickerLayout)
                .setIsSmartLockEnabled(false)
                .setAvailableProviders(providers)
                .build(), LOGIN_REQUEST_CODE);
    }

    private void delaySplashScreen() {

        progressBar = findViewById(R.id.progress_bar);

        progressBar.setVisibility(View.VISIBLE);

        Completable.timer(3, TimeUnit.SECONDS, AndroidSchedulers.mainThread()).subscribe(new Action() {
            @Override
            public void run() throws Exception {
                firebaseAuth.addAuthStateListener(listener);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == LOGIN_REQUEST_CODE) {
            IdpResponse response = IdpResponse.fromResultIntent(data);
            if(resultCode == RESULT_OK) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            } else {
                Toast.makeText(this, "[ERROR] : " + response.getError().getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

}