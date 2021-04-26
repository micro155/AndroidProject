package com.example.academyapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class AddChattingActivity extends AppCompatActivity {

    public static final String MESSAGES_CHILD = "chat_messages";

    private ListView academy_listView;
    private DatabaseReference mFirebaseDatabase;
    private AcademyListViewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_chatting);

        academy_listView = (ListView) findViewById(R.id.academy_list_view);
        mFirebaseDatabase = FirebaseDatabase.getInstance().getReference(Common.ACADEMY_INFO_REFERENCE);

        final ArrayList<String> name_list = new ArrayList<String>();
        final ArrayList<String> address_list = new ArrayList<String>();

        long now = System.currentTimeMillis();
        Date nowDate = new Date(now);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        final String getTime = simpleDateFormat.format(nowDate);

        mFirebaseDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
//                    String key = dataSnapshot.getKey();
                    String address = dataSnapshot.child("academy_address").getValue(String.class);
                    String academy = dataSnapshot.child("academy_name").getValue(String.class);

                    name_list.add(academy);
                    address_list.add(address);

                    Log.d("dataAdapter", "adapter : " + snapshot.getChildren());

                    adapter = new AcademyListViewAdapter(AddChattingActivity.this, name_list, address_list);
                    adapter.notifyDataSetChanged();
                    academy_listView.setAdapter(adapter);

                    academy_listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                            final Intent intent = new Intent(getApplicationContext(), ChattingActivity.class);
                            intent.putExtra("academy_name", name_list.get(position));
                            final DatabaseReference chatList = FirebaseDatabase.getInstance().getReference("ChatRoom");

                            DatabaseReference normal_info = FirebaseDatabase.getInstance().getReference(Common.MEMBER_INFO_REFERENCE);
                            String normal_user = FirebaseAuth.getInstance().getCurrentUser().getUid();

                            normal_info.child(normal_user).child("nickName").addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    String nickName = snapshot.getValue(String.class);
                                    intent.putExtra("normal_name", nickName);

//                                    chatList.child("director")
//                                            .setValue(name_list.get(position));
//                                            .addOnFailureListener(new OnFailureListener() {
//                                                @Override
//                                                public void onFailure(@NonNull Exception e) {
//                                                    Toast.makeText(AddChattingActivity.this, "채팅방 생성 실패", Toast.LENGTH_SHORT);
//                                                    Log.d("make room Fail Log", "reason : " + e.getMessage());
//                                                }
//                                            }).addOnSuccessListener(new OnSuccessListener<Void>() {
//                                        @Override
//                                        public void onSuccess(Void aVoid) {
//                                            Toast.makeText(AddChattingActivity.this, "채팅방 생성", Toast.LENGTH_SHORT);
//                                        }
//                                    });

                                    chatList.child(name_list.get(position)).child(nickName).child(MESSAGES_CHILD).setValue(getTime);
//                                            .setValue(nickName);

//                                    chatList.child("normal_member")
//                                            .setValue(nickName);
//                                            .addOnFailureListener(new OnFailureListener() {
//                                                @Override
//                                                public void onFailure(@NonNull Exception e) {
//                                                    Toast.makeText(AddChattingActivity.this, "채팅방 생성 실패", Toast.LENGTH_SHORT);
//                                                    Log.d("make room Fail Log", "reason : " + e.getMessage());
//                                                }
//                                            }).addOnSuccessListener(new OnSuccessListener<Void>() {
//                                        @Override
//                                        public void onSuccess(Void aVoid) {
//                                            Toast.makeText(AddChattingActivity.this, "채팅방 생성", Toast.LENGTH_SHORT);
//                                        }
//                                    });

                                    chatList.child(nickName).child(name_list.get(position)).child(MESSAGES_CHILD).setValue(getTime);
//                                            .setValue(name_list.get(position));
                                    startActivity(intent);
//                                            .addOnFailureListener(new OnFailureListener() {
//                                                @Override
//                                                public void onFailure(@NonNull Exception e) {
//                                                    Toast.makeText(AddChattingActivity.this, "채팅방 생성 실패", Toast.LENGTH_SHORT);
//                                                    Log.d("make room Fail Log", "reason : " + e.getMessage());
//                                                }
//                                            }).addOnSuccessListener(new OnSuccessListener<Void>() {
//                                        @Override
//                                        public void onSuccess(Void aVoid) {
//                                            Toast.makeText(AddChattingActivity.this, "채팅방 생성", Toast.LENGTH_SHORT);
//                                            startActivity(intent);
//                                        }
//                                    });
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });

//                            startActivity(intent);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AddChattingActivity.this, error.getCode(), Toast.LENGTH_SHORT);
            }
        });

    }

}