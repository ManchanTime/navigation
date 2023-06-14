package com.gachon.innergation.activity;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.gachon.innergation.R;
import com.gachon.innergation.adapter.WifiAdapter;
import com.gachon.innergation.dialog.CustomDialog;
import com.gachon.innergation.info.GetWifiInfo;
import com.gachon.innergation.info.WifiInfo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class GetDataActivity extends AppCompatActivity {

    private CustomDialog customProgressDialog;
    private TextView textName, textX, textY;
    private Button btnStart, btnUpload;
    private IntentFilter intentFilter = new IntentFilter();
    private WifiManager wifiManager;
    boolean isPermitted = false;
    final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private ArrayList<GetWifiInfo> wifiList = new ArrayList<>();
    private ArrayList<GetWifiInfo> input = new ArrayList<>();
    private RecyclerView recyclerView;
    private WifiAdapter wifiAdapter;
    private LinearLayoutManager layoutManager;
    private String name;
    private int count;

    // BroadcastReceiver 정의
    // 여기서는 이전 예제에서처럼 별도의 Java class 파일로 만들지 않았는데, 어떻게 하든 상관 없음
    BroadcastReceiver wifiScanReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context c, Intent intent) {
            // wifiManager.startScan(); 시  발동되는 메소드 ( 예제에서는 버튼을 누르면 startScan()을 했음. )
            boolean success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false); //스캔 성공 여부 값 반환
            if (success) {
                scanSuccess();
            } else {
                scanFailure();
            }
        }// onReceive()..
    };

    private void scanSuccess() {    // Wifi검색 성공
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            customProgressDialog.cancel();
            return;
        }
        List<ScanResult> results = wifiManager.getScanResults();
        for (int i = 0; i < results.size(); i++) {
            ScanResult result = results.get(i);
            if(!result.BSSID.contains("94:64"))
                continue;
            wifiList.add(new GetWifiInfo(result.SSID, result.BSSID, result.level));
        }
        Collections.sort(wifiList);
        for(int i=0;i<10;i++){
            input.add(wifiList.get(i));
        }
        wifiAdapter = new WifiAdapter(this, wifiList);
        recyclerView.setAdapter(wifiAdapter);
        customProgressDialog.cancel();
    }

    private void scanFailure() {    // Wifi검색 실패
    }

    @Override
    protected void onPause(){
        super.onPause();
        try {
            this.unregisterReceiver(wifiScanReceiver);
        } catch (IllegalArgumentException e){

        } catch (Exception e) {}
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_data);

        requestRuntimePermission();
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        getApplicationContext().registerReceiver(wifiScanReceiver, intentFilter);
        // wifi가 활성화되어있는지 확인 후 꺼져 있으면 켠다
        if(!wifiManager.isWifiEnabled())
            wifiManager.setWifiEnabled(true);

        //로딩창 객체 생성
        customProgressDialog = new CustomDialog(this);
        customProgressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        customProgressDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        btnStart = findViewById(R.id.btn_refresh);
        btnStart.setOnClickListener(onClickListener);
        btnUpload = findViewById(R.id.btn_upload);
        btnUpload.setOnClickListener(onClickListener);
        textName = findViewById(R.id.text_name);
        textX = findViewById(R.id.text_x);
        textY = findViewById(R.id.text_y);
        Intent get = getIntent();
        if(get != null){
            name = get.getStringExtra("className");
            textName.setText(name);
            textX.setText("x : 0");
            textY.setText("y : 0");
        }

        recyclerView = findViewById(R.id.recycler_data);
        recyclerView.setHasFixedSize(true);
        wifiAdapter = new WifiAdapter(this, wifiList);
        wifiAdapter.setHasStableIds(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

    }

    View.OnClickListener onClickListener = (v) -> {
        switch(v.getId()){
            case R.id.btn_refresh:
                if(isPermitted) {
                    customProgressDialog.show();
                    //화면터치 방지
                    customProgressDialog.setCanceledOnTouchOutside(false);
                    //뒤로가기 방지
                    customProgressDialog.setCancelable(false);
                    wifiList.clear();
                    input.clear();
                    // wifi 스캔 시작
                    boolean start = wifiManager.startScan();
                    if (start) {
                        Toast.makeText(this, "success", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "fail", Toast.LENGTH_SHORT).show();
                        customProgressDialog.cancel();
                    }
                } else {
                    Toast.makeText(getApplicationContext(),
                            "Location access 권한이 없습니다..", Toast.LENGTH_LONG).show();
                    finishAffinity();
                }
                btnStart.setText("Refresh");
                btnUpload.setEnabled(true);
                break;
            case R.id.btn_upload:
                customProgressDialog.show();
                //화면터치 방지
                customProgressDialog.setCanceledOnTouchOutside(false);
                //뒤로가기 방지
                customProgressDialog.setCancelable(false);
                getDataTest();
                break;
        }
    };

    //허용하시겠습니까? 퍼미션 창 뜨게하는 것!
    private void requestRuntimePermission() {
        if (ContextCompat.checkSelfPermission(GetDataActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(GetDataActivity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
            } else {
                ActivityCompat.requestPermissions(GetDataActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            }
        } else {
            isPermitted = true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // ACCESS_FINE_LOCATION 권한을 얻음
                    isPermitted = true;

                } else {
                    // 권한을 얻지 못 하였으므로 location 요청 작업을 수행할 수 없다
                    // 적절히 대처한다
                    isPermitted = false;

                }
            }
        }
    }

    public void setting(){
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        DocumentReference docRef = firebaseFirestore.collection("classrooms").document(name);
        docRef.update("RSSI", input).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        customProgressDialog.cancel();
                    }
                });
        customProgressDialog.cancel();
        finish();
    }


    public void getDataTest(){
        setting();
    }
}