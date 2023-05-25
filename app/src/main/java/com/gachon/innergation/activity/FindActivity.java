package com.gachon.innergation.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import com.gachon.innergation.R;
import com.gachon.innergation.info.GetWifiInfo;
import com.gachon.innergation.info.InputWifiInfo;
import com.gachon.innergation.info.WifiInfo;
import com.google.firebase.firestore.FirebaseFirestore;

public class FindActivity extends AppCompatActivity {

    TextView textName;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find);

        textName = findViewById(R.id.text_name);
        Intent get = getIntent();
        textName.setText(get.getStringExtra("className"));
        db = FirebaseFirestore.getInstance();
    }

    // 1. 요청으로 들어온 wifi 신호 (bssid, rssi, ssid 3개로 이루어진 리스트)와 DB에 있는 데이터를 brute-force로 비교
    // 2. 같은 bssid 값이 있으면 신호 세기의 차(절댓값)의 합을 구하고, bssid가 일치하는 수만큼 나눠줘서 평균도 구함
    // 3. 위에서 구한 객체가 calc이고, 이 calc들의 리스트인 calc_list를 만듦.
    // 4. calc_list를 크기 순으로 정렬하고, 제일 큰 1개를 뽑음
    protected void bruteForce(InputWifiInfo input) {
        for (GetWifiInfo wifiInfo : input.getWifiInfos()) {

        }
    }
}