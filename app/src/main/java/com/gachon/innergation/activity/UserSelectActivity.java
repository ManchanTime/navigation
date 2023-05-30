package com.gachon.innergation.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import com.gachon.innergation.R;
import com.gachon.innergation.adapter.ClassAdapter;
import com.google.firebase.firestore.FirebaseFirestore;

import org.checkerframework.checker.units.qual.A;
import org.checkerframework.checker.units.qual.C;

import java.util.ArrayList;

public class UserSelectActivity extends AppCompatActivity {

    private FirebaseFirestore firebaseFirestore;
    private ProgressDialog customProgressDialog;
    private Button btnCurrent, btnSearch;
    private RecyclerView recyclerView;
    private ClassAdapter classAdapter;
    private ArrayList<String> classrooms;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_select);

        //로딩창 객체 생성
        customProgressDialog = new ProgressDialog(this);
        customProgressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        customProgressDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        firebaseFirestore = FirebaseFirestore.getInstance();
        btnCurrent = findViewById(R.id.btn_current);
        btnSearch = findViewById(R.id.btn_search);
        btnCurrent.setOnClickListener(onClickListener);
        btnSearch.setOnClickListener(onClickListener);
    }

    View.OnClickListener onClickListener = (v) -> {
        switch(v.getId()){
            case R.id.btn_current:
                Intent intent = new Intent(this, FindActivity.class);
                intent.putExtra("order", "current");
                startActivity(intent);
                break;
            case R.id.btn_search:
                intent = new Intent(this, UserSearchActivity.class);
                startActivity(intent);
                break;
        }
    };
}