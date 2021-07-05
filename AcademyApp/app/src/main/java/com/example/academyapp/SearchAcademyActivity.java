package com.example.academyapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class SearchAcademyActivity extends AppCompatActivity {

    private ListView academy_list_view;
    private DatabaseReference mFirebaseDatabase;
    private AcademyListViewAdapter adapter;
    private TextView empty_search_academy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_academy);

        academy_list_view = (ListView) findViewById(R.id.academy_list_view_contracts);
        empty_search_academy = (TextView) findViewById(R.id.empty_search_academy);

        final AutoCompleteTextView auto_academy_search = (AutoCompleteTextView) findViewById(R.id.autoSearch_academy_contracts);
        mFirebaseDatabase = FirebaseDatabase.getInstance().getReference(Common.ACADEMY_INFO_REFERENCE);

        final ArrayList<String> academy_list = new ArrayList<String>();
        final ArrayList<String> address_list = new ArrayList<String>();

        mFirebaseDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                boolean check = false;

                for (DataSnapshot list_snapshot : snapshot.getChildren()) {
                    String academy_name = list_snapshot.child("academy_name").getValue(String.class);
                    String academy_address = list_snapshot.child("acadmy_address").getValue(String.class);

                    if (academy_name != null) {

                        check = true;

                        academy_list.add(academy_name);
                        address_list.add(academy_address);

                        adapter = new AcademyListViewAdapter(SearchAcademyActivity.this, academy_list, address_list);
                        adapter.notifyDataSetChanged();
                        academy_list_view.setAdapter(adapter);
                        auto_academy_search.setAdapter(new ArrayAdapter<String>(SearchAcademyActivity.this, android.R.layout.simple_dropdown_item_1line, academy_list));

                        academy_list_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                Intent intent = new Intent(getApplicationContext(), DownloadContentsActivity.class);
                                intent.putExtra("academy_name", academy_list.get(position));
                                setResult(RESULT_OK, intent);
                                finish();
                            }
                        });
                    }
                }

                if (!check) {
                    empty_search_academy.setVisibility(View.VISIBLE);
                } else {
                    empty_search_academy.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }



}