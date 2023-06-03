package com.gachon.innergation.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.gachon.innergation.R;
import com.gachon.innergation.dialog.CustomDialog;
import com.gachon.innergation.info.GetWifiInfo;
import com.gachon.innergation.service.DrawMap;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class FindActivity extends AppCompatActivity {

    TextView textName;
    private CustomDialog customProgressDialog;
    final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private IntentFilter intentFilter = new IntentFilter();

    //와이파이 리스트
    private ArrayList<GetWifiInfo> wifiList = new ArrayList<>();
    //비교 개수
    private int count = 0;
    //비교 시 4개이상 동일한게 없다면 리스트에 넣어서 제일 비슷한걸ㄹ
    private String result;
    //비교할 bssid 꺼내기
    private ArrayList<String> comp = new ArrayList<>();
    //퍼미션
    boolean isPermitted = false;
    private WifiManager wifiManager;
    private Button btnNow;
    //목표 위치
    private String order;

    private static int[][] maps;

    // BroadcastReceiver 정의
    // 여기서는 이전 예제에서처럼 별도의 Java class 파일로 만들지 않았는데, 어떻게 하든 상관 없음
    BroadcastReceiver wifiScanReceiverNow = new BroadcastReceiver() {
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
        comp.clear();
        List<ScanResult> results = wifiManager.getScanResults();
        for (int i = 0; i < results.size(); i++) {
            ScanResult result = results.get(i);
            if(!result.BSSID.contains("94:64"))
                continue;
            wifiList.add(new GetWifiInfo(result.SSID, result.BSSID, result.level));
        }

        Collections.sort(wifiList);

        for(int i=0;i<5;i++){
            comp.add(wifiList.get(i).getBssid());
            Log.e("test",wifiList.get(i).getSsid() + " " + wifiList.get(i).getBssid()+" " + wifiList.get(i).getRssi());
        }

        set_up();
    }

    private void scanFailure() {    // Wifi검색 실패
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewEx viewEx = new ViewEx(this);
        setContentView(R.layout.activity_find);

        requestRuntimePermission();
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        getApplicationContext().registerReceiver(wifiScanReceiverNow, intentFilter);
        // wifi가 활성화되어있는지 확인 후 꺼져 있으면 켠다
        if(wifiManager.isWifiEnabled() == false)
            wifiManager.setWifiEnabled(true);

        //로딩창 객체 생성
        customProgressDialog = new CustomDialog(this);
        customProgressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        customProgressDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        Intent get = getIntent();
        //order = get.getStringExtra("order");
        btnNow = findViewById(R.id.btn_find);
        btnNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isPermitted) {
                    customProgressDialog.show();
                    //화면터치 방지
                    customProgressDialog.setCanceledOnTouchOutside(false);
                    //뒤로가기 방지
                    customProgressDialog.setCancelable(false);
                    wifiList.clear();
                    // wifi 스캔 시작
                    boolean start = wifiManager.startScan();
                    if(start){
                        Toast.makeText(FindActivity.this,"success",Toast.LENGTH_SHORT).show();
                        set_up();
                    }else{
                        Toast.makeText(FindActivity.this,"fail",Toast.LENGTH_SHORT).show();
                        customProgressDialog.cancel();
                    }
                } else {
                    Toast.makeText(getApplicationContext(),
                            "Location access 권한이 없습니다..", Toast.LENGTH_LONG).show();
                    finishAffinity();
                }
            }
        });
        textName = findViewById(R.id.text_name);
        // Astar 테스트용. 정적으로 입력된 값을 액티비티 생성 시 출력만 해준다.

        setUpMap();
        DrawMap.draw(maps);

    }

    private void setUpMap() {
        maps = new int[][]{
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 1, 1, 0, 0, 0, 0, 0, 0, 1, 1, 1, 0, 0},
                {0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0},
                {0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0},
                {0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0}
        };
    }

    // 스캔을 완료했을떄, 스캔한 값으로 현재 강의실 이름을 받아오는 좌표.
    // 강의실 이름을 다 받아오면 Astar 경로 출력을 해보는 테스트를 임의로 진행해보겠다.
    public void set_up(){
        customProgressDialog.cancel();
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        CollectionReference collectionReference = firebaseFirestore.collection("classrooms");
        collectionReference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    int best = 0;
                    for(QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                        ArrayList<String> test = new ArrayList<>();
                        ArrayList<Object> get = (ArrayList<Object>) documentSnapshot.getData().get("RSSI");
                        for(int i=0;i<5;i++){
                            HashMap<String, String> data = (HashMap<String, String>) get.get(i);
//                            Log.e("SSID", data.get("ssid"));
//                            Log.e("BSSID", data.get("bssid"));
//                            Log.e("RSSI",String.valueOf(data.get("rssi")));
                            test.add(data.get("bssid"));
                            if(comp.contains(data.get("bssid"))){
                                count++;
                            }
                        }
                        //4개 이상 동일시 그냥 현재위치로 추정
                        if(count >= 3) {
                            //textName.setText(documentSnapshot.getData().get("class").toString());
                            int tmp = 0;
                            for(int i=0;i<comp.size();i++){
                                if(test.get(i).equals(comp.get(i))){
                                    tmp++;
                                }
                            }
                            if(best < tmp){
                                best = tmp;
                                Log.e("b",best+"");
                                result = documentSnapshot.getData().get("class").toString();
                                Log.e("test", result);
                            }
                        }
                    }
                    textName.setText(result);
                    count = 0;
                }
            }
        });
    }

    protected class ViewEx extends View{
        public ViewEx(Context context)
        {
            super(context);
        }
        public void onDraw(Canvas canvas)
        {
            canvas.drawColor(Color.BLACK);

            Paint MyPaint = new Paint();
            MyPaint.setStrokeWidth(5f);
            MyPaint.setStyle(Paint.Style.FILL);
            MyPaint.setColor(Color.GRAY);
            canvas.drawLine(0,0,360,640,MyPaint);
        }
    }


    //허용하시겠습니까? 퍼미션 창 뜨게하는 것!
    private void requestRuntimePermission() {
        if (ContextCompat.checkSelfPermission(FindActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(FindActivity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
            } else {
                ActivityCompat.requestPermissions(FindActivity.this,
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

}