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
import com.gachon.innergation.info.MapInfo;
import com.gachon.innergation.info.Node;
import com.gachon.innergation.service.DrawMap;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class FindActivity extends AppCompatActivity {

    TextView textName;
    private Node sourceNode;
    private Node destNode;
    private String sourceName;
    private String destinationName;
    private String filePath;
    private FirebaseFirestore firebaseFirestore;
    private CustomDialog customProgressDialog;
    final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private IntentFilter intentFilter = new IntentFilter();

    //와이파이 리스트
    private ArrayList<GetWifiInfo> wifiList = new ArrayList<>();
    //비교 개수
    private int count = 0;
    //비교 시 4개이상 동일한게 없다면 리스트에 넣어서 제일 비슷한걸로
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
        Intent intent = getIntent();
        destinationName = intent.getStringExtra("className");
        firebaseFirestore = FirebaseFirestore.getInstance();
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
        setUpMap();
        filePath = getApplicationContext().getFilesDir().getPath().toString();
        String mapPath = filePath + "/AstarMap.txt";
        try (PrintWriter writer = new PrintWriter(mapPath)) {
            for (int[] row : maps) {
                writer.println(Arrays.toString(row));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        setSourceCoord();

    }

    // Map을 기본적으로 모두 1 (이동불가)로 설정해두고, 이동할 수 있는 경로만 0으로 변경해줌.
    private void setUpMap() {
        int startX = 26, startY = 10;
//        int endX = 72, endY = 84;
        int endX = 42, endY = 37;
        int cnt = 0;
        boolean cntFlag = false;
        maps = new int[100][100];
        for (int i = 0; i < maps.length; i++) {
            for (int y = 0; y < maps[i].length; y++) {
                maps[i][y] = 1;
            }
        }

        for (int x = 0; x < maps.length; x++) {
            for (int y = 0; y < maps[x].length; y++) {
//                (7,85)  (87,85)
//                (7,88)  (87,88) - 삼각형 제외
//                => 제일 아래 직사각형
                if (x >= 7 && x <= 87 && y >= 85 && y <= 88) {
                    maps[y][x] = 0;
                }

//                (20, 5)   (22, 5)
//                (20, 88)   (22, 88)
//                => 아르테크네에서 내려오는 직선 복도
                if (x >= 20 && x <= 22 && y >= 5 && y <= 88) {
                    maps[y][x] = 0;
                }

//                (20, 37)   (44, 37)
//                (20, 44)   (44, 44)
//                => 중간 엘레베이터 직사각형
                if (x >= 22 && x <= 42 && y >= 37 && y <= 44) {
                    maps[y][x] = 0;
                }

//                (31, 75)   (40, 75)
//                (31, 84)   (40, 84)
//                => 아래 엘레베이터 직사각형
                if (x >= 31 && x <= 40 && y >= 75 && y <= 84) {
                    maps[y][x] = 0;
                }

//                (22, 75)   (63, 75)
//                (22, 76)   (63, 76)
//                => 아래 엘레베이터 위 412호와 405호를 잇는 직사각형
                if (x >= 22 && x <= 63 && y >= 75 && y <= 76) {
                    maps[y][x] = 0;
                }

//                (6, 5)   (25, 5)
//                (6, 10)   (25, 10)
//                => 아르테크네
                if (x >= 6 && x <= 26 && y >= 5 && y <= 10) {
                    maps[y][x] = 0;
                }

                if (x >= 45 && x <= 49 && y == 44) {
                    maps[y][x] = 0;
                }
                if (x >= 46 && x <= 49 && y == 45) {
                    maps[y][x] = 0;
                }
                if (x >= 47 && x <= 50 && y == 46) {
                    maps[y][x] = 0;
                }
                if (x >= 47 && x <= 50 && y == 47) {
                    maps[y][x] = 0;
                }
                if (x >= 48 && x <= 50 && y == 48) {
                    maps[y][x] = 0;
                }
                if (x >= 49 && x <= 51 && y == 49) {
                    maps[y][x] = 0;
                }
                if (x >= 49 && x <= 52 && y == 50) {
                    maps[y][x] = 0;
                }
                if (x >= 50 && x <= 52 && y == 51) {
                    maps[y][x] = 0;
                }
                if (x >= 50 && x <= 53 && y == 52) {
                    maps[y][x] = 0;
                }
                if (x >= 51 && x <= 53 && y == 53) {
                    maps[y][x] = 0;
                }
                if (x >= 51 && x <= 54 && y == 54) {
                    maps[y][x] = 0;
                }
                if (x >= 52 && x <= 54 && y == 55) {
                    maps[y][x] = 0;
                }
                if (x >= 53 && x <= 55 && y == 56) {
                    maps[y][x] = 0;
                }
                if (x >= 53 && x <= 56 && y == 57) {
                    maps[y][x] = 0;
                }
                if (x >= 54 && x <= 57 && y == 58) {
                    maps[y][x] = 0;
                }
                if (x >= 55 && x <= 57 && y == 59) {
                    maps[y][x] = 0;
                }
                if (x >= 55 && x <= 58 && y == 60) {
                    maps[y][x] = 0;
                }
                if (x >= 56 && x <= 58 && y == 61) {
                    maps[y][x] = 0;
                }
                if (x >= 56 && x <= 59 && y == 62) {
                    maps[y][x] = 0;
                }
                if (x >= 57 && x <= 60 && y == 63) {
                    maps[y][x] = 0;
                }
                if (x >= 58 && x <= 60 && y == 64) {
                    maps[y][x] = 0;
                }
                if (x >= 58 && x <= 61 && y == 65) {
                    maps[y][x] = 0;
                }
                if (x >= 59 && x <= 61 && y == 66) {
                    maps[y][x] = 0;
                }
                if (x >= 60 && x <= 62 && y == 67) {
                    maps[y][x] = 0;
                }
                if (x >= 60 && x <= 62 && y == 68) {
                    maps[y][x] = 0;
                }
                if (x >= 61 && x <= 63 && y == 69) {
                    maps[y][x] = 0;
                }
                if (x >= 61 && x <= 63 && y == 70) {
                    maps[y][x] = 0;
                }
                if (x >= 62 && x <= 64 && y == 71) {
                    maps[y][x] = 0;
                }
                if (x >= 62 && x <= 65 && y == 72) {
                    maps[y][x] = 0;
                }
                if (x >= 63 && x <= 65 && y == 73) {
                    maps[y][x] = 0;
                }
                if (x >= 64 && x <= 66 && y == 74) {
                    maps[y][x] = 0;
                }
                if (x >= 64 && x <= 67 && y == 75) {
                    maps[y][x] = 0;
                }
                if (x >= 64 && x <= 67 && y == 76) {
                    maps[y][x] = 0;
                }
                if (x >= 65 && x <= 67 && y == 77) {
                    maps[y][x] = 0;
                }
                if (x >= 65 && x <= 68 && y == 78) {
                    maps[y][x] = 0;
                }
                if (x >= 67 && x <= 69 && y == 79) {
                    maps[y][x] = 0;
                }
                if (x >= 67 && x <= 69 && y == 80) {
                    maps[y][x] = 0;
                }
                if (x >= 68 && x <= 70 && y == 81) {
                    maps[y][x] = 0;
                }
                if (x >= 68 && x <= 71 && y == 82) {
                    maps[y][x] = 0;
                }
                if (x >= 69 && x <= 71 && y == 83) {
                    maps[y][x] = 0;
                }
                if (x >= 68 && x <= 71 && y == 84) {
                    maps[y][x] = 0;
                }


                // y가 10일때 우리는 26,27,28 만 찍어야 함
                // 근데 지금은 y가 10일때 26부터 70까지를 다 찍어버림
//                if(y == startY && startY <= endY && startX <= endX) {
//                    for(int k=0; k<4; k++) {
//                        maps[y + k][startX] = 0;
//                    }
//                    cntFlag = !cntFlag;
//                    if(!cntFlag) {
//                        cnt++;
//                        if(cnt % 2 == 0) {
//                            startY++;
//                        } else {
//                            startX++;
//                            startY++;
//                        }
////                        startY++;
//                    } else {
//                        startX++;
//                        startY++;
//                    }
//                }
            }
        }
    }

    // 스캔을 완료했을떄, 스캔한 값으로 현재 강의실 이름을 받아오는 좌표.
    // 강의실 이름을 다 받아오면 Astar 경로 출력을 해보는 테스트를 임의로 진행해보겠다.
    public void set_up(){
        customProgressDialog.cancel();
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
                    // 여기서 출발지가 결정된다.
                    sourceName = result;
                    count = 0;
                }
            }
        });
        // 일단은 정적으로 값을 넣어두겠다.
        sourceName = "412";
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

    private void setSourceCoord() {
        DocumentReference docRef = firebaseFirestore.collection("classroom_coordinate").document("413");
        docRef.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            String coordinates = documentSnapshot.getString("value");
                            if (coordinates != null) {
                                String[] values = coordinates.split(",");
                                if (values.length == 2) {
                                    String xValue = values[0];
                                    String yValue = values[1];
                                    Log.e("TAG", "x값 : " + xValue);
                                    sourceNode = new Node(Integer.parseInt(yValue), Integer.parseInt(xValue));
                                    setDestCoord(destinationName);
                                }
                            }
                        } else {
                            // 도큐먼트가 존재하지 않을 경우 처리
                            Log.e("TAG", "도큐먼트 존재 x");
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("TAG", "onFailure: " + e);
                    }
                });

    }

    private void setDestCoord(String dest) {
        DocumentReference docRef = firebaseFirestore.collection("classroom_coordinate").document(dest);
        docRef.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            String coordinates = documentSnapshot.getString("value");
                            if (coordinates != null) {
                                String[] values = coordinates.split(",");
                                if (values.length == 2) {
                                    String xValue = values[0];
                                    String yValue = values[1];
                                    Log.e("TAG", "x값 : " + xValue);
                                    destNode = new Node(Integer.parseInt(yValue), Integer.parseInt(xValue));
                                    DrawMap.draw(filePath, maps, sourceNode, destNode, getApplicationContext());
                                }
                            }
                        } else {
                            // 도큐먼트가 존재하지 않을 경우 처리
                            Log.e("TAG", "도큐먼트 존재 x");
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("TAG", "onFailure: " + e);
                    }
                });

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