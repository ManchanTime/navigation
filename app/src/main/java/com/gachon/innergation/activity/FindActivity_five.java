package com.gachon.innergation.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.gachon.innergation.R;
import com.gachon.innergation.dialog.CustomDialog;
import com.gachon.innergation.info.GetWifiInfo;
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

public class FindActivity_five extends AppCompatActivity {

    private Node sourceNode;
    private Node destNode;
    private String sourceName = "";
    private String destinationName;
    private String filePath;
    private FirebaseFirestore firebaseFirestore;
    private CustomDialog customProgressDialog;
    final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private IntentFilter intentFilter;

    //와이파이 리스트
    private ArrayList<GetWifiInfo> wifiList = new ArrayList<>();
    //비교 개수
    private int count = 0;
    //비교 시 4개이상 동일한게 없다면 리스트에 넣어서 제일 비슷한걸로
    private String result;
    private String previous;
    //비교할 bssid 꺼내기
    private ArrayList<String> comp = new ArrayList<>();
    //퍼미션
    boolean isPermitted = false;
    private WifiManager wifiManager;

    private static int[][] maps;

    private TextView textView;

    private ImageView imageView;
    private Bitmap bitmap;
    private Bitmap mutableBitmap;
    private Canvas canvas;
    private ArrayList<Node> getPaths = new ArrayList<>();
    private Node currentPoint;
    private boolean update;

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

        for(int i=0;i<10;i++){
            comp.add(wifiList.get(i).getBssid());
        }
        set_up();
    }

    private void scanFailure() {}

    @Override
    protected void onPause(){
        super.onPause();
        try {
            this.unregisterReceiver(wifiScanReceiverNow);
            Toast.makeText(this, "end", Toast.LENGTH_SHORT).show();
        } catch (IllegalArgumentException e){

        } catch (Exception e) {}
    }

    @Override
    public void onBackPressed() {
        unregisterReceiver(wifiScanReceiverNow);
        Toast.makeText(this, "end", Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    protected void onResume(){
        super.onResume();
        //측정 시작
        intentFilter = new IntentFilter();
        BackgroundTask thread = new BackgroundTask();
        requestRuntimePermission();
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        thread.execute();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        registerReceiver(wifiScanReceiverNow, intentFilter);
        // wifi가 활성화되어있는지 확인 후 꺼져 있으면 켠다
        if(wifiManager.isWifiEnabled() == false) {
            wifiManager.setWifiEnabled(true);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_five);

        // canvas를 액티비티 생성 시점에 하나만 생성해서 재활용 하겠음 (canvas 중복 draw 방지)
        imageView = findViewById(R.id.view1);
        bitmap = ((BitmapDrawable)imageView.getDrawable()).getBitmap();
        mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        canvas = new Canvas(mutableBitmap);

        textView = findViewById(R.id.textView);
        //목적지 받아오기
        Intent intent = getIntent();
        if(intent != null) {
            destinationName = intent.getStringExtra("className");
        }
        firebaseFirestore = FirebaseFirestore.getInstance();

        //로딩창 객체 생성
        customProgressDialog = new CustomDialog(this);
        customProgressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        customProgressDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        setUpMap();
        filePath = getApplicationContext().getFilesDir().getPath().toString();
        String mapPath = filePath + "/AstarMapFive.txt";
        try (PrintWriter writer = new PrintWriter(mapPath)) {
            for (int[] row : maps) {
                writer.println(Arrays.toString(row));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        //sourceName = "512";
        //setSourceCoord();
    }

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

//                => 제일 아래 직사각형

                if (x >= 8 && x <= 84 && y >= 84 && y <= 86) {
                    maps[y][x] = 0;
                }

//                => 아르테크네에서 내려오는 직선 복도
                if (x >= 20 && x <= 22 && y >= 11 && y <= 88) {
                    maps[y][x] = 0;
                }

//                => 중간 엘레베이터 직사각형 (오류 날 시 수정)
                if (x >= 20 && x <= 43 && y >= 33 && y <= 36) {
                    maps[y][x] = 0;
                }

//                => 아래 엘레베이터 직사각형
                if (x >= 30 && x <= 38 && y >= 74 && y <= 86) {
                    maps[y][x] = 0;
                }

//                => 아래 엘레베이터 위 412호와 405호를 잇는 직사각형
                if (x >= 20 && x <= 64 && y >= 74 && y <= 75) {
                    maps[y][x] = 0;
                }

//                => 아르테크네
                if (x >= 6 && x <= 28 && y >= 5 && y <= 11) {
                    maps[y][x] = 0;
                }

//                => 큐브
                if (x >= 37 && x <= 40 && y == 54) {
                    maps[y][x] = 0;
                }

                if (x >= 35 && x <= 53 && y == 55) {
                    maps[y][x] = 0;
                }
                if (x >= 31 && x <= 54 && y == 56) {
                    maps[y][x] = 0;
                }
                if (x >= 31 && x <= 46 && y == 57) {
                    maps[y][x] = 0;
                }
                if (x >= 32 && x <= 45 && y == 58) {
                    maps[y][x] = 0;
                }
                if (x >= 30 && x <= 46 && y == 59) {
                    maps[y][x] = 0;
                }
                if (x >= 26 && x <= 44 && y == 60) {
                    maps[y][x] = 0;
                }
                if (x >= 23 && x <= 42 && y == 61) {
                    maps[y][x] = 0;
                }
                if (x >= 23 && x <= 28 && y == 62) {
                    maps[y][x] = 0;
                }
                if (x >= 23 && x <= 24 && y == 63) {
                    maps[y][x] = 0;
                }

                if (x >= 26 && x <= 29 && y == 12) {
                    maps[y][x] = 0;
                }
                if (x >= 27 && x <= 29 && y == 13) {
                    maps[y][x] = 0;
                }
                if (x >= 28 && x <= 30 && y == 14) {
                    maps[y][x] = 0;
                }
                if (x >= 28 && x <= 31 && y == 15) {
                    maps[y][x] = 0;
                }
                if (x >= 29 && x <= 31 && y == 16) {
                    maps[y][x] = 0;
                }
                if (x >= 29 && x <= 32 && y == 17) {
                    maps[y][x] = 0;
                }
                if (x >= 30 && x <= 32 && y == 18) {
                    maps[y][x] = 0;
                }
                if (x >= 30 && x <= 33 && y == 19) {
                    maps[y][x] = 0;
                }
                if (x >= 31 && x <= 33 && y == 20) {
                    maps[y][x] = 0;
                }
                if (x >= 32 && x <= 34 && y == 21) {
                    maps[y][x] = 0;
                }
                if (x >= 32 && x <= 35 && y == 22) {
                    maps[y][x] = 0;
                }
                if (x >= 33 && x <= 35 && y == 23) {
                    maps[y][x] = 0;
                }
                if (x >= 33 && x <= 36 && y == 24) {
                    maps[y][x] = 0;
                }
                if (x >= 34 && x <= 36 && y == 25) {
                    maps[y][x] = 0;
                }
                if (x >= 35 && x <= 37 && y == 26) {
                    maps[y][x] = 0;
                }
                if (x >= 35 && x <= 37 && y == 27) {
                    maps[y][x] = 0;
                }
                if (x >= 36 && x <= 38 && y == 28) {
                    maps[y][x] = 0;
                }
                if (x >= 36 && x <= 39 && y == 29) {
                    maps[y][x] = 0;
                }
                if (x >= 37 && x <= 39 && y == 30) {
                    maps[y][x] = 0;
                }
                if (x >= 38 && x <= 40 && y == 31) {
                    maps[y][x] = 0;
                }
                if (x >= 38 && x <= 40 && y == 32) {
                    maps[y][x] = 0;
                }

                if (x >= 39 && x <= 43 && y == 37) {
                    maps[y][x] = 0;
                }
                if (x >= 40 && x <= 44 && y == 38) {
                    maps[y][x] = 0;
                }
                if (x >= 41 && x <= 44 && y == 39) {
                    maps[y][x] = 0;
                }
                if (x >= 41 && x <= 45 && y == 40) {
                    maps[y][x] = 0;
                }
                if (x >= 42 && x <= 45 && y == 41) {
                    maps[y][x] = 0;
                }
                if (x >= 42 && x <= 46 && y == 42) {
                    maps[y][x] = 0;
                }
                if (x >= 43 && x <= 46 && y == 43) {
                    maps[y][x] = 0;
                }
                if (x >= 44 && x <= 47 && y == 44) {
                    maps[y][x] = 0;
                }
                if (x >= 45 && x <= 48 && y == 45) {
                    maps[y][x] = 0;
                }
                if (x >= 45 && x <= 48 && y == 46) {
                    maps[y][x] = 0;
                }
                if (x >= 45 && x <= 48 && y == 47) {
                    maps[y][x] = 0;
                }
                if (x >= 46 && x <= 49 && y == 48) {
                    maps[y][x] = 0;
                }
                if (x >= 47 && x <= 50 && y == 49) {
                    maps[y][x] = 0;
                }
                if (x >= 48 && x <= 51 && y == 50) {
                    maps[y][x] = 0;
                }
                if (x >= 48 && x <= 51 && y == 51) {
                    maps[y][x] = 0;
                }
                if (x >= 49 && x <= 51 && y == 52) {
                    maps[y][x] = 0;
                }
                if (x >= 49 && x <= 52 && y == 53) {
                    maps[y][x] = 0;
                }
                if (x >= 47 && x <= 53 && y == 54) {
                    maps[y][x] = 0;
                }



//                 =>  대각선
                if (x >= 49 && x <= 53 && y == 55) {
                    maps[y][x] = 0;
                }
                if (x >= 50 && x <= 55 && y == 56) {
                    maps[y][x] = 0;
                }
                if (x >= 51 && x <= 55 && y == 57) {
                    maps[y][x] = 0;
                }
                if (x >= 52 && x <= 55 && y == 58) {
                    maps[y][x] = 0;
                }
                if (x >= 52 && x <= 56 && y == 59) {
                    maps[y][x] = 0;
                }
                if (x >= 53 && x <= 56 && y == 60) {
                    maps[y][x] = 0;
                }
                if (x >= 54 && x <= 57 && y == 61) {
                    maps[y][x] = 0;
                }
                if (x >= 54 && x <= 57 && y == 62) {
                    maps[y][x] = 0;
                }
                if (x >= 55 && x <= 58 && y == 63) {
                    maps[y][x] = 0;
                }
                if (x >= 55 && x <= 58 && y == 64) {
                    maps[y][x] = 0;
                }
                if (x >= 56 && x <= 59 && y == 65) {
                    maps[y][x] = 0;
                }
                if (x >= 56 && x <= 60 && y == 66) {
                    maps[y][x] = 0;
                }
                if (x >= 57 && x <= 60 && y == 67) {
                    maps[y][x] = 0;
                }
                if (x >= 58 && x <= 60 && y == 68) {
                    maps[y][x] = 0;
                }
                if (x >= 58 && x <= 61 && y == 69) {
                    maps[y][x] = 0;
                }
                if (x >= 59 && x <= 62 && y == 70) {
                    maps[y][x] = 0;
                }
                if (x >= 60 && x <= 62 && y == 71) {
                    maps[y][x] = 0;
                }
                if (x >= 60 && x <= 62 && y == 72) {
                    maps[y][x] = 0;
                }
                if (x >= 61 && x <= 64 && y == 73) {
                    maps[y][x] = 0;
                }
                if (x >= 60 && x <= 65 && y == 75) {
                    maps[y][x] = 0;
                }
                if (x >= 62 && x <= 65 && y == 76) {
                    maps[y][x] = 0;
                }
                if (x >= 63 && x <= 66 && y == 77) {
                    maps[y][x] = 0;
                }
                if (x >= 63 && x <= 67 && y == 78) {
                    maps[y][x] = 0;
                }
                if (x >= 64 && x <= 67 && y == 79) {
                    maps[y][x] = 0;
                }
                if (x >= 65 && x <= 68 && y == 80) {
                    maps[y][x] = 0;
                }
                if (x >= 66 && x <= 68 && y == 81) {
                    maps[y][x] = 0;
                }
                if (x >= 65 && x <= 69 && y == 82) {
                    maps[y][x] = 0;
                }
                if (x >= 62 && x <= 69 && y == 83) {
                    maps[y][x] = 0;
                }

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
                    int best_count = 0;
                    int best = 0;
                    for(QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                        ArrayList<String> test = new ArrayList<>();
                        ArrayList<Object> get = (ArrayList<Object>) documentSnapshot.getData().get("RSSI");
                        for(int i=0;i<10;i++){
                            HashMap<String, String> data = (HashMap<String, String>) get.get(i);
                            test.add(data.get("bssid"));
                            if(comp.contains(data.get("bssid"))){
                                count++;
                            }
                        }
                        //4개 이상 동일시 그냥 현재위치로 추정
                        if(best_count < count){
                            best_count = count;
                            result = documentSnapshot.getData().get("class").toString();
                        }
                        count = 0;
                    }
                    if(result.equals("5_floor_elevator_right") || result.equals("5_floor_elevator_right_left")){
                        textView.setText("현재 위치\n" + "엘리베이터");
                    }else
                        textView.setText("현재 위치\n" + result);
                    if(destinationName == null)
                        destinationName = "null";
                    // 여기서 출발지가 결정된다.
                    sourceName = result;
                    if(previous == null || !previous.equals(result)) {
                        previous = result;
                        setSourceCoord();
                    }
                }
            }
        });
        // 일단은 정적으로 값을 넣어두겠다.
        //sourceName = "512";
    }

    private class BackgroundTask extends AsyncTask<Integer, Integer, Integer> {
        // 백그라운드 작업
        protected Integer doInBackground(Integer... integers) {
            while (sourceName != null && !sourceName.equals(destinationName)) {
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                wifiList.clear();
                // wifi 스캔 시작
                wifiManager.startScan();
            }
            publishProgress(0);
            return 0;
        }

        // 중간중간 프로그레스 퍼센트를 업데이트 해준다.
        protected void onProgressUpdate(Integer... progress) {

        }

        // 작업이 모두 끝나면 Dialog를 띄워 준다.
        protected void onPostExecute(Integer result) {
            showToast("목적지에 도착했습니다.");
        }
    }

    private void showToast(String message){
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        AlertDialog.Builder builder = new AlertDialog.Builder(FindActivity_five.this);
        builder.setTitle("완료");
        builder.setMessage("목적지에 도착했습니다. 안내를 종료하시겠습니까?");
        builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });
        builder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    //허용하시겠습니까? 퍼미션 창 뜨게하는 것!
    private void requestRuntimePermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            }
        } else {
            isPermitted = true;
        }
    }

    private void setSourceCoord() {
        DocumentReference docRef = firebaseFirestore.collection("classroom_coordinate").document(sourceName);
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
                                    // 사용자의 출발지를 확인했으면 기존에 만들어진 canvas를 클리어 해준다.
                                    clearCanvas();
                                    drawPoint(0, sourceNode.coord.y, sourceNode.coord.x);
                                    currentPoint = sourceNode;
                                    if(!destinationName.equals("null"))
                                        setDestCoord();
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

    private void setDestCoord() {
        DocumentReference docRef = firebaseFirestore.collection("classroom_coordinate").document(destinationName);
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
                                    if(!update){
                                        drawPoint(2, destNode.coord.y, destNode.coord.x);
                                        update = true;
                                    }
                                    DrawMap.draw(filePath, maps, sourceNode, destNode, getApplicationContext());
                                    getPaths = DrawMap.getPaths();
//                                    ImageView view1 = findViewById(R.id.view1);
                                    for(int i=0;i<getPaths.size();i++){
                                        Node startPoint = getPaths.get(i);
                                        if(i+1 < getPaths.size()) {
                                            Node endPoint = getPaths.get(i + 1);
                                            if(getPaths.size() != 2) {
                                                drawLine(0, startPoint.coord.y, startPoint.coord.x, endPoint.coord.y, endPoint.coord.x);
                                            }
                                        }
                                    }
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
        if (requestCode == MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {// If request is cancelled, the result arrays are empty.
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

    //현재위치 점찍기?
    public void drawPoint(int mode, float x, float y){
        Paint paint = new Paint();
        if(mode == 0) {
            paint.setColor(Color.GREEN);
            paint.setStrokeWidth(20f);
        }
        else if(mode == 1){
            paint.setColor(Color.parseColor("#F5F5F5"));
            paint.setStrokeWidth(22f);
        }else{
            paint.setColor(Color.BLUE);
            paint.setStrokeWidth(30f);
        }
        float startXPos = (x / 100) * canvas.getWidth();
        float startYPos = (y / 100) * canvas.getHeight();
        canvas.drawPoint(startXPos, startYPos, paint);
        imageView.setImageBitmap(mutableBitmap);
    }

    public void drawLine(int mode, float startX, float startY, float endX, float endY) {
//        Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
//        Bitmap mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
//
//        Canvas canvas = new Canvas(mutableBitmap);
        Paint paint = new Paint();
        if(mode == 0) {
            paint.setColor(Color.RED);
            paint.setStrokeWidth(7.0f);
        }
        else {
            paint.setColor(Color.parseColor("#F5F5F5"));
            paint.setStrokeWidth(7.8f);
        }
        paint.setStyle(Paint.Style.STROKE);

        // 상대적인 위치를 기준으로 선을 그립니다.
        float startXPos = (startX / 100) * canvas.getWidth();
        float startYPos = (startY / 100) * canvas.getHeight();
        float endXPos = (endX / 100) * canvas.getWidth();
        float endYPos = (endY / 100) * canvas.getHeight();

        canvas.drawLine(startXPos, startYPos, endXPos, endYPos, paint);

        imageView.setImageBitmap(mutableBitmap);
    }

    private void clearCanvas() {
        imageView.findViewById(R.id.view1);
        if(currentPoint != null)
            drawPoint(1, currentPoint.coord.y, currentPoint.coord.x);
        for(int i=0;i<getPaths.size();i++){
            Node startPoint = getPaths.get(i);
            if(i+1 < getPaths.size()) {
                Node endPoint = getPaths.get(i + 1);
                if(getPaths.size() != 2) {
                    drawLine(1, startPoint.coord.y, startPoint.coord.x, endPoint.coord.y, endPoint.coord.x);
                }
            }
        }
        getPaths.clear();
    }
}