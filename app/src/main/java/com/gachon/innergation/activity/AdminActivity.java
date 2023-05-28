package com.gachon.innergation.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.gachon.innergation.R;

public class AdminActivity extends AppCompatActivity {

    private Button btnFour, btnFive;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        btnFour = findViewById(R.id.btn_four);
        btnFour.setOnClickListener(onClickListener);
        btnFive = findViewById(R.id.btn_five);
        btnFive.setOnClickListener(onClickListener);
    }

    View.OnClickListener onClickListener = (v) -> {
        Intent intent = null;
        switch (v.getId()){
            case R.id.btn_four:
                intent = new Intent(this, FloorFourActivity.class);
                break;
            case R.id.btn_five:
                intent = new Intent(this, FloorFiveActivity.class);
                break;
        }
        startActivity(intent);
    };
}