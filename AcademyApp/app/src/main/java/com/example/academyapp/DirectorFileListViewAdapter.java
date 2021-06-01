package com.example.academyapp;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

import static android.os.Environment.DIRECTORY_DOWNLOADS;

public class DirectorFileListViewAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<String> file_list;
    private OnFileDeleteClickListener listener;
    private String file_name;
    private String academy_name;

    public interface OnFileDeleteClickListener {
        void onFileDelete (String fileName, String academy_name);
    }

    public DirectorFileListViewAdapter(Context context, ArrayList<String> file_list, String academy_name, OnFileDeleteClickListener listener) {
        this.context = context;
        this.file_list = file_list;
        this.academy_name = academy_name;
        this.listener = listener;
    }

    @Override
    public int getCount() {
        return file_list.size();
    }

    @Override
    public Object getItem(int i) {
        return file_list.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.director_file_list_base, parent, false);
        }

        final TextView file_view = (TextView) convertView.findViewById(R.id.upload_file_name);
        Button btn_file_delete = (Button) convertView.findViewById(R.id.upload_file_delete);

        file_name = file_list.get(position);

        Log.d("FileListViewAdapter TAG", "file_name : " + file_name);
        file_view.setText(file_name);



        btn_file_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String fileName = file_list.get(position);
                listener.onFileDelete(fileName, academy_name);
            }
        });

        return convertView;
    }

    public void delete_File(final String fileName, String academy_name) {


        final StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl("gs://academyapp-d7c41.appspot.com").child("videos/" + fileName);
        final DatabaseReference file_data_ref = FirebaseDatabase.getInstance().getReference("FileList").child(academy_name).child(fileName);

        Log.d("fileName", "filename : " + fileName);


        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setTitle("강의 영상 삭제")
                .setMessage(fileName + "을(를) 삭제하시겠습니까?")
                .setPositiveButton("예", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        storageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                file_data_ref.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        file_list.remove(fileName);
                                        setItemList(file_list);
                                        Toast.makeText(context, "강의 영상 삭제 완료", Toast.LENGTH_SHORT).show();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(context, e + "로 인한 삭제 실패", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(context, e + "로 인한 삭제 실패", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                })
                .setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(context, "삭제 취소", Toast.LENGTH_SHORT).show();
                    }
                });
        AlertDialog alertDialog = builder.create();

        alertDialog.show();
    }

    public void setItemList(ArrayList<String> file_list) {
        this.file_list = file_list;
        notifyDataSetChanged();
    }
}

