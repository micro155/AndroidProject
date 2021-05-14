package com.example.academyapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.renderscript.Sampler;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class DownloaderListViewAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<String> downloader_nickName_list;
    private ArrayList<String> downloader_phone_list;
    private String academy_name;
    private OnDeleteClickListener deleteClickListener;
    private String downloader_nickName;
    private String downloader_phone;
    private String downloader_name;
    private String academy_code;

    public interface OnDeleteClickListener {
        void onDelete (String downloader_nickName, String academy_name);
    }

    public DownloaderListViewAdapter(Context context, ArrayList<String> downloader_nickName_list, ArrayList<String> downloader_phone_list, String academy_name, String downloader_name, String academy_code, OnDeleteClickListener deleteClickListener) {
        this.context = context;
        this.downloader_nickName_list = downloader_nickName_list;
        this.downloader_phone_list = downloader_phone_list;
        this.academy_name = academy_name;
        this.deleteClickListener = deleteClickListener;
        this.downloader_name = downloader_name;
        this.academy_code = academy_code;
    }

    @Override
    public int getCount() {
        return downloader_nickName_list.size();
    }

    @Override
    public Object getItem(int i) {
        return downloader_nickName_list.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.downloader_management_list_base, parent, false);
        }

        final TextView downloader_nickName_view = (TextView) convertView.findViewById(R.id.downloader_name_list);
        final TextView downloader_phone_view = (TextView) convertView.findViewById(R.id.downloader_phone_list);
        Button btn_downloader_delete = (Button) convertView.findViewById(R.id.downloader_delete);

        downloader_nickName = downloader_nickName_list.get(position);
        downloader_phone = downloader_phone_list.get(position);

        downloader_nickName_view.setText(downloader_nickName);
        downloader_phone_view.setText(downloader_phone);

        downloader_nickName_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("nickName click", "click OK");

                androidx.appcompat.app.AlertDialog.Builder detail_dialog = new androidx.appcompat.app.AlertDialog.Builder(context);
                View detail_info_view = LayoutInflater.from(context).inflate(R.layout.layout_detail_downloader_info, null);

                TextView downloader_nickName_view = (TextView) detail_info_view.findViewById(R.id.downloader_detail_nickName);
                TextView downloader_name_view = (TextView) detail_info_view.findViewById(R.id.downloader_detail_downloader_name);
                TextView downloader_phone_view = (TextView) detail_info_view.findViewById(R.id.downloader_detail_downloader_phone);
                TextView downloader_academy_code_view = (TextView) detail_info_view.findViewById(R.id.downloader_detail_academy_code);

                Button downloader_detail_confirm_button = (Button) detail_info_view.findViewById(R.id.downloader_detail_confirm);

                downloader_nickName_view.setText(downloader_nickName);
                downloader_name_view.setText(downloader_name);
                downloader_phone_view.setText(downloader_phone);
                downloader_academy_code_view.setText(academy_code);

                detail_dialog.setView(detail_info_view);

                final androidx.appcompat.app.AlertDialog dialog = detail_dialog.create();
                dialog.show();

                downloader_detail_confirm_button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
            }
        });

        downloader_phone_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("phone click", "click OK");

                androidx.appcompat.app.AlertDialog.Builder detail_dialog = new androidx.appcompat.app.AlertDialog.Builder(context);
                View detail_info_view = LayoutInflater.from(context).inflate(R.layout.layout_detail_downloader_info, null);

                TextView downloader_nickName_view = (TextView) detail_info_view.findViewById(R.id.downloader_detail_nickName);
                TextView downloader_name_view = (TextView) detail_info_view.findViewById(R.id.downloader_detail_downloader_name);
                TextView downloader_phone_view = (TextView) detail_info_view.findViewById(R.id.downloader_detail_downloader_phone);
                TextView downloader_academy_code_view = (TextView) detail_info_view.findViewById(R.id.downloader_detail_academy_code);

                Button downloader_detail_confirm_button = (Button) detail_info_view.findViewById(R.id.downloader_detail_confirm);

                downloader_nickName_view.setText(downloader_nickName);
                downloader_name_view.setText(downloader_name);
                downloader_phone_view.setText(downloader_phone);
                downloader_academy_code_view.setText(academy_code);

                detail_dialog.setView(detail_info_view);

                final androidx.appcompat.app.AlertDialog dialog = detail_dialog.create();
                dialog.show();

                downloader_detail_confirm_button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
            }
        });

        btn_downloader_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                String downloader_name = downloader_name_list.get(position);
                deleteClickListener.onDelete(downloader_nickName, academy_name);
            }
        });

        return convertView;
    }

    public void deleteDownloader(final String downloader_nickName, final String academy_name) {

        AlertDialog.Builder confirmDeleteDialog = new AlertDialog.Builder(context);
        final DatabaseReference normal_user_ref = FirebaseDatabase.getInstance().getReference("Contracts");


        confirmDeleteDialog.setTitle("회원 삭제")
                .setMessage("해당 회원을 정말로 삭제하시겠습니까?")
                .setPositiveButton("예", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        normal_user_ref.child(downloader_nickName).getRef().removeValue().addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(context, e.getMessage() + " 로 인한 삭제 실패", Toast.LENGTH_SHORT).show();
                            }
                        });

                        normal_user_ref.child(academy_name).getRef().removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                downloader_nickName_list.remove(downloader_nickName);
                                downloader_phone_list.remove(downloader_phone);
                                setItemList(downloader_nickName_list, downloader_phone_list);
                                Toast.makeText(context, "해당 회원이 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(context, e.getMessage() + " 로 인한 삭제 실패", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }).setNegativeButton("아니오", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(context, "삭제 취소", Toast.LENGTH_SHORT).show();
            }
        });

        AlertDialog alertDialog = confirmDeleteDialog.create();

        alertDialog.show();

    }

    public void setItemList(ArrayList<String> downloader_nickName_list, ArrayList<String> downloader_phone_list) {
        this.downloader_nickName_list = downloader_nickName_list;
        this.downloader_phone_list = downloader_phone_list;
        notifyDataSetChanged();
    }
}
