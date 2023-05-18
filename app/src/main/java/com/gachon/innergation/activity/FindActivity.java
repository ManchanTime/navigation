package com.gachon.innergation.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.gachon.innergation.R;

public class FindActivity extends AppCompatActivity {

    TextView textName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find);

        textName = findViewById(R.id.text_name);
        Intent get = getIntent();
        textName.setText(get.getStringExtra("className"));
    }
}