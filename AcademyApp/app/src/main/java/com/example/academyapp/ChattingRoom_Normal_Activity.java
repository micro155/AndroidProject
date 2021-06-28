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
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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

public class ChattingRoom_Normal_Activity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 100;

    private DrawerLayout drawer;
    private AppBarConfiguration mAppBarConfiguration;
    private NavController navController;
    private NavigationView navigationView;

    private AlertDialog waitingDialog;
    private StorageReference storageReference;
    private ImageView img_profile;
    private Uri imageUri;
    private ChatRoomListViewAdapter adapter;
    private ArrayList<String> name_list;
    private ArrayList<String> messages_array;
    private ArrayList<String> profile;
    private ListView listView;
    private TextView empty_chatting;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatting_room_normal);

        Toolbar toolbar = findViewById(R.id.toolbar_chatting_room_normal);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.menu_chatting);

        drawer = findViewById(R.id.drawer_chatting_room_normal_layout);

        navigationView = findViewById(R.id.nav_chatting_room_normal_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.

        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_normalmember_home, R.id.nav_normalmember_logout, R.id.nav_download, R.id.nav_chatting)
                .setDrawerLayout(drawer)
                .build();
        navController = Navigation.findNavController(this, R.id.nav_chatting_room_normal_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        listView = (ListView) findViewById(R.id.chatting_room_list_view);
        empty_chatting = (TextView) findViewById(R.id.empty_chatting);

        init();

        final String normal_uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference normal_ref = FirebaseDatabase.getInstance().getReference(Common.MEMBER_INFO_REFERENCE);

        normal_ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String uid = dataSnapshot.child("uid").getValue(String.class);

                    if (uid != null) {
                        if (uid.equals(normal_uid)) {
                            String nick_name = dataSnapshot.child("nickName").getValue(String.class);
                            showNormalChattingRoomList(nick_name);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
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
                    AlertDialog.Builder builder = new AlertDialog.Builder(ChattingRoom_Normal_Activity.this);
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
                                    Intent intent = new Intent(ChattingRoom_Normal_Activity.this, SplashScreenActivity.class);
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
                                    .setTextColor(ContextCompat.getColor(ChattingRoom_Normal_Activity.this, android.R.color.holo_red_dark));
                            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                                    .setTextColor(ContextCompat.getColor(ChattingRoom_Normal_Activity.this, R.color.colorAccent));
                        }
                    });
                    dialog.show();
                } else if (item.getItemId() == R.id.nav_download) {
                    Intent intent = new Intent(ChattingRoom_Normal_Activity.this, DownloadContentsActivity.class);
                    startActivity(intent);
                    finish();
                } else if (item.getItemId() == R.id.nav_normalmember_home) {
                    Intent intent = new Intent(ChattingRoom_Normal_Activity.this, NormalMemberHomeActivity.class);
                    startActivity(intent);
                    finish();
                } else if (item.getItemId() == R.id.nav_chatting) {
                    Intent intent = new Intent(ChattingRoom_Normal_Activity.this, ChattingRoom_Normal_Activity.class);
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

    private void showNormalChattingRoomList(final String normal_nickName) {
        name_list = new ArrayList<String>();
        messages_array = new ArrayList<String>();
        profile = new ArrayList<String>();


        DatabaseReference normal_chat_ref = FirebaseDatabase.getInstance().getReference("ChatRoom").child(normal_nickName);

        normal_chat_ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                boolean check = false;

                for (DataSnapshot list_data : snapshot.getChildren()) {
                    final String director_list = list_data.getKey();
                    final String[] director_profile = {null};
                    final String[] chat_text = {null};
                    final String[] name = {null};

                    if (director_list != null) {

                        check = true;

                        DatabaseReference token_ref = FirebaseDatabase.getInstance().getReference("ChatRoom").child(normal_nickName).child(director_list).child("chat_messages");

                        token_ref.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                name_list.clear();
                                profile.clear();
                                messages_array.clear();

                                for (DataSnapshot chat_list : snapshot.getChildren()) {

                                    chat_text[0] = chat_list.child("text").getValue(String.class);
                                    name[0] = chat_list.child("name").getValue(String.class);

                                    Log.d("chat list", "list : " + chat_text[0]);
                                    Log.d("name list", "list : " + name[0]);

                                    if (director_list.equals(name[0])) {
                                        director_profile[0] = chat_list.child("photoURL").getValue(String.class);
                                        Log.d("director_profile list", "list : " + director_profile[0]);
                                    }
                                }

                                name_list.add(director_list);
                                profile.add(director_profile[0]);
                                messages_array.add(chat_text[0]);

                                adapter = new ChatRoomListViewAdapter(ChattingRoom_Normal_Activity.this, name_list, messages_array, profile);
                                adapter.notifyDataSetChanged();

                                listView.setAdapter(adapter);
                                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                        Intent intent = new Intent(getApplicationContext(), ChattingActivity.class);
                                        intent.putExtra("academy_name", name_list.get(position));
                                        intent.putExtra("director_profile", profile.get(position));
                                        intent.putExtra("normal_name", normal_nickName);
                                        intent.putExtra("user_type", "일반회원");
                                        startActivity(intent);
                                    }
                                });

                                listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                                    @Override
                                    public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {

                                        final DatabaseReference chat_ref = FirebaseDatabase.getInstance().getReference("ChatRoom");

                                        AlertDialog.Builder builder = new AlertDialog.Builder(ChattingRoom_Normal_Activity.this);

                                        builder.setTitle("대화방 나가기")
                                                .setMessage("대화방을 나가시겠습니까?")
                                                .setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        Toast.makeText(getApplicationContext(), "대화방 나가기 취소", Toast.LENGTH_SHORT).show();
                                                    }
                                                })
                                                .setPositiveButton("예", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        chat_ref.child(normal_nickName).child(name_list.get(position)).removeValue().addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                Toast.makeText(getApplicationContext(), e.getMessage() + "로 인한 대화방 삭제 실패", Toast.LENGTH_SHORT).show();
                                                            }
                                                        }).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                name_list.remove(name_list.get(position));
                                                                profile.remove(profile.get(position));
                                                                messages_array.remove(messages_array.get(position));
                                                                setChatList(name_list, profile, messages_array);
                                                                adapter.notifyDataSetChanged();
                                                                Toast.makeText(getApplicationContext(), "대화방 삭제 완료", Toast.LENGTH_SHORT).show();
                                                            }
                                                        });
                                                    }
                                                });

                                        AlertDialog alertDialog = builder.create();
                                        alertDialog.show();

                                        return true;
                                    }
                                });
                                
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                }

                if (!check) {
                    empty_chatting.setVisibility(View.VISIBLE);
                } else {
                    empty_chatting.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void setChatList(ArrayList<String> name_list, ArrayList<String> profile, ArrayList<String> messages) {
        this.name_list = name_list;
        this.profile = profile;
        this.messages_array = messages;
    }


    private void showDialogUpload() {
        final DatabaseReference normal_ref = FirebaseDatabase.getInstance().getReference(Common.MEMBER_INFO_REFERENCE);
        final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        AlertDialog.Builder builder = new AlertDialog.Builder(ChattingRoom_Normal_Activity.this);
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
        getMenuInflater().inflate(R.menu.chatting_room_normal_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_chatting: {
                Intent intent = new Intent(this, AddChattingActivity.class);
                startActivity(intent);
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_chatting_room_normal_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

}