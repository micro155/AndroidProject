package com.example.academyapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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

import com.example.academyapp.Model.FileListInfo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static android.os.Environment.DIRECTORY_DOWNLOADS;

public class FileListViewAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<String> academy_list;
    private ArrayList<String> file_list;
    private OnDownloadClickListener mlistener;
    private String academy_name;
    private String file_name;

    public interface OnDownloadClickListener {
        void onDownload (String fileName);
    }

    public FileListViewAdapter(Context context, ArrayList<String> file_list, ArrayList<String> academy_list, OnDownloadClickListener listener) {
        this.context = context;
        this.academy_list = academy_list;
        this.file_list = file_list;
        this.mlistener = listener;
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
            convertView = inflater.inflate(R.layout.dropdown_file_list, parent, false);
        }

        final TextView academy_view = (TextView) convertView.findViewById(R.id.file_academy);
        final TextView file_view = (TextView) convertView.findViewById(R.id.file_name);
        Button btn_download = (Button) convertView.findViewById(R.id.file_download);

        file_name = file_list.get(position);
        academy_name = academy_list.get(position);

        academy_view.setText(academy_name);
        file_view.setText(file_name);


        btn_download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fileName = file_list.get(position);
                mlistener.onDownload(fileName);
            }
        });

        return convertView;
    }

    public void download_File(final String fileName) {


        FirebaseStorage storage = FirebaseStorage.getInstance();
        final StorageReference storageReference = storage.getReference();


        Log.d("fileName", "filename : " + fileName);
        

        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setTitle("강의 다운로드")
                .setMessage(fileName + "를 다운로드하시겠습니까?")
                .setPositiveButton("예", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        storageReference.child("videos/" + fileName).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String url = uri.toString();
                                download_func(context, fileName, DIRECTORY_DOWNLOADS, url);
                                Toast.makeText(context, "다운로드 진행중", Toast.LENGTH_SHORT).show();
                                Log.d("url address", "url : " + url);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(context, e + "로 인한 다운로드 실패", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                })
                .setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(context, "다운로드 취소", Toast.LENGTH_SHORT).show();
                    }
                });
        AlertDialog alertDialog = builder.create();

        alertDialog.show();
    }

    private void download_func(final Context context, String fileName, String destinationDirectory, String url) {

        final DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);

        Uri uri = Uri.parse(url);
        DownloadManager.Request request = new DownloadManager.Request(uri);

        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalFilesDir(context, destinationDirectory, fileName);

        final long download_managerId = downloadManager.enqueue(request);

        final ProgressDialog progressBarDialog = new ProgressDialog(context);
        progressBarDialog.setTitle("다운로드 중");

        progressBarDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressBarDialog.setProgress(0);

        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean downloading = true;

                while (downloading) {
                    DownloadManager.Query q = new DownloadManager.Query();
                    q.setFilterById(download_managerId);
                    Cursor cursor = downloadManager.query(q);
                    cursor.moveToFirst();
                    int bytes_downloaded = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                    int bytes_total = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));

                    if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL) {
                        downloading = false;
                    }
                    final int dl_progress = (int) ((bytes_downloaded * 100l) / bytes_total);

                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            progressBarDialog.setProgress((int) dl_progress);
                            if (dl_progress == 100) {
                                progressBarDialog.dismiss();
                            }
                        }
                    });
                    cursor.close();
                }
            }
        }).start();

        progressBarDialog.show();

    }
}
