package com.example.academyapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AddChattingActivity extends AppCompatActivity {

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

        mFirebaseDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String key = dataSnapshot.getKey();
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
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            Intent intent = new Intent(getApplicationContext(), ChattingActivity.class);
                            intent.putExtra("academy_name", name_list.get(position));
                            intent.putExtra("academy_address", address_list.get(position));
                            startActivity(intent);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }


}