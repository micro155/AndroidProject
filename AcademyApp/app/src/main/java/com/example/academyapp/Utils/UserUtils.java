package com.example.academyapp.Utils;

import android.view.View;

import androidx.annotation.NonNull;

import com.example.academyapp.Common;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Map;

public class UserUtils {
    public static void updateUser(final View view, Map<String, Object> updateData) {
        FirebaseDatabase.getInstance()
                .getReference(Common.MEMBER_INFO_REFERENCE)
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .updateChildren(updateData)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Snackbar.make(view, e.getMessage(), Snackbar.LENGTH_SHORT).show();
                    }
                }).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Snackbar.make(view, "정보 업데이트가 완료되었습니다.", Snackbar.LENGTH_SHORT).show();
            }
        });
    }
}
