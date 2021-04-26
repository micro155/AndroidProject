package com.example.academyapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.renderscript.Sampler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.academyapp.Model.ChatMessage;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.auth.api.Auth;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChattingActivity extends AppCompatActivity {


    private FirebaseRecyclerAdapter<ChatMessage, MessageViewHolder> mFirebaseAdapter;

    public static final String MESSAGES_CHILD = "chat_messages";
    private DatabaseReference mFirebaseDatabaseReference;

    private EditText mMessageEditText;
    private RecyclerView mMessageRecyclerView;
    private String normal_username;
    private String director_username;
    private String mPhotoUrl;
    private String chatUser;

    private FirebaseUser mFirebaseUser;
    private DatabaseReference user_name;
    private String Uid;

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;
        TextView messageTextView;
        CircleImageView photoImageView;
        ImageView messageImageView;

        public MessageViewHolder(View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            messageImageView = itemView.findViewById(R.id.messageImageView);
            messageTextView = itemView.findViewById(R.id.messageTextView);
            photoImageView = itemView.findViewById(R.id.photoImageView);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatting);

        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
        mMessageEditText = findViewById(R.id.message_edit);
        mMessageRecyclerView = findViewById(R.id.chat_message);

        chatUser = FirebaseAuth.getInstance().getCurrentUser().getUid();

        Intent intent = getIntent();
        final String academy_name = intent.getStringExtra("academy_name");
        final String normal_name = intent.getStringExtra("normal_name");
        String director_profile = intent.getStringExtra("director_profile");
        String normal_profile = intent.getStringExtra("normal_profile");


        Log.d("academy_name", "name : " + academy_name);

        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (mFirebaseUser.getPhotoUrl() != null) {
            mPhotoUrl = mFirebaseUser.getPhotoUrl().toString();
        }

        Uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        user_name = FirebaseDatabase.getInstance().getReference(Common.MEMBER_INFO_REFERENCE).child(Uid);

        user_name.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String type = snapshot.child("type").getValue(String.class);
                normal_username = snapshot.child("nickName").getValue(String.class);

                if (type.equals("일반회원")) {

                    findViewById(R.id.send_button).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            final ChatMessage chatMessage = new ChatMessage(mMessageEditText.getText().toString(), normal_username, mPhotoUrl);

                            mFirebaseDatabaseReference.child("ChatRoom").child(academy_name).child(normal_username).child(MESSAGES_CHILD).push().setValue(chatMessage);

                            mFirebaseDatabaseReference.child("ChatRoom").child(normal_username).child(academy_name).child(MESSAGES_CHILD).push().setValue(chatMessage);
                            mMessageEditText.setText("");
                        }
                    });

                } else {
                    final DatabaseReference academy = FirebaseDatabase.getInstance().getReference(Common.ACADEMY_INFO_REFERENCE).child(Uid).child("academy_name");

                    academy.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            director_username = snapshot.getValue(String.class);

                            findViewById(R.id.send_button).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    final ChatMessage chatMessage = new ChatMessage(mMessageEditText.getText().toString(), director_username, mPhotoUrl);

                                    Log.d("director", "director name: " + normal_name);

                                    mFirebaseDatabaseReference.child("ChatRoom").child(director_username).child(normal_name).child(MESSAGES_CHILD).push().setValue(chatMessage);

                                    mFirebaseDatabaseReference.child("ChatRoom").child(normal_name).child(director_username).child(MESSAGES_CHILD).push().setValue(chatMessage);
                                    mMessageEditText.setText("");
                                }
                            });
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        if (academy_name != null) {

            Query query = mFirebaseDatabaseReference.child("ChatRoom").child(normal_name).child(academy_name).child(MESSAGES_CHILD);
            FirebaseRecyclerOptions<ChatMessage> options = new FirebaseRecyclerOptions.Builder<ChatMessage>().setQuery(query, ChatMessage.class).build();

            mFirebaseAdapter = new FirebaseRecyclerAdapter<ChatMessage, MessageViewHolder>(options) {
                @NonNull
                @Override
                public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_view, parent, false);
                    return new MessageViewHolder(view);
                }

                @Override
                protected void onBindViewHolder(MessageViewHolder holder, int position, ChatMessage model) {
                    if (model.getText() != null && model.getName() != null) {
                        holder.messageTextView.setText(model.getText());
                        holder.nameTextView.setText(model.getName());
                    }
                    if (model.getPhotoURL() == null) {
                        holder.photoImageView.setImageDrawable(ContextCompat.getDrawable(ChattingActivity.this, R.drawable.ic_baseline_account_circle_24));
                    } else {
                        Glide.with(ChattingActivity.this).load(model.getPhotoURL()).into(holder.photoImageView);
                    }
                }
            };

            mMessageRecyclerView.setLayoutManager(new LinearLayoutManager(ChattingActivity.this));
            mMessageRecyclerView.setAdapter(mFirebaseAdapter);

        } else if (normal_name != null) {
            Query query = mFirebaseDatabaseReference.child("ChatRoom").child(academy_name).child(normal_name).child(MESSAGES_CHILD);
            FirebaseRecyclerOptions<ChatMessage> options = new FirebaseRecyclerOptions.Builder<ChatMessage>().setQuery(query, ChatMessage.class).build();

            mFirebaseAdapter = new FirebaseRecyclerAdapter<ChatMessage, MessageViewHolder>(options) {
                @NonNull
                @Override
                public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_view, parent, false);
                    return new MessageViewHolder(view);
                }

                @Override
                protected void onBindViewHolder(MessageViewHolder holder, int position, ChatMessage model) {
                    if (model.getText() != null && model.getName() != null) {
                        holder.messageTextView.setText(model.getText());
                        holder.nameTextView.setText(model.getName());
                    }
                    if (model.getPhotoURL() == null) {
                        holder.photoImageView.setImageDrawable(ContextCompat.getDrawable(ChattingActivity.this, R.drawable.ic_baseline_account_circle_24));
                    } else {
                        Glide.with(ChattingActivity.this).load(model.getPhotoURL()).into(holder.photoImageView);
                    }
                }
            };

            mMessageRecyclerView.setLayoutManager(new LinearLayoutManager(ChattingActivity.this));
            mMessageRecyclerView.setAdapter(mFirebaseAdapter);
        }


    }

    @Override
    protected void onStart() {
        super.onStart();
        mFirebaseAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mFirebaseAdapter.stopListening();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chatting_option_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId())
        {
            case R.id.chatting_exit: {
                Intent intent = new Intent (this, ChattingRoomActivity.class);
                startActivity(intent);
                finish();
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }



}