package com.gachon.innergation.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.gachon.innergation.R;

public class MainActivity extends AppCompatActivity {

    Button btnAdmin, btnUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnAdmin = findViewById(R.id.btn_admin);
        btnUser = findViewById(R.id.btn_user);
        btnAdmin.setOnClickListener(onClickListener);
        btnUser.setOnClickListener(onClickListener);
    }

    View.OnClickListener onClickListener = (v) -> {
        Intent intent = null;
        switch(v.getId()){
            case R.id.btn_admin:
                intent = new Intent(this, AdminActivity.class);
                break;
            case R.id.btn_user:
                intent = new Intent(this, UserSelectActivity.class);
                break;
        }
        startActivity(intent);
    };
}