package com.gachon.innergation.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.gachon.innergation.R;
import com.gachon.innergation.info.BestCalc;
import com.gachon.innergation.info.Calc;
import com.gachon.innergation.info.GetWifiInfo;
import com.gachon.innergation.info.GetWifiInfoList;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class FindActivity extends AppCompatActivity {

    TextView textName;
    private String order;

    private String TAG = "test";
    private GetWifiInfoList getWifiInfoList;

    private BestCalc bestCalc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewEx viewEx = new ViewEx(this);
        setContentView(R.layout.activity_find);
        Intent get = getIntent();
        order = get.getStringExtra("order");
        set_up();
    }

    public void set_up() {

        // 임시로 선언한 input data
        List<GetWifiInfo> input = new ArrayList<>();
        input.add(new GetWifiInfo("eduroam", "94:64:24:a0:cb:20", -57));
        input.add(new GetWifiInfo("GC_free_WiFi", "94:64:24:a0:cb:22", -58));
        input.add(new GetWifiInfo("eduroam", "94:64:24:9e:d1:40", -59));
        input.add(new GetWifiInfo("eduroam", "94:64:24:9e:21:90", -59));
        input.add(new GetWifiInfo("GC_free_WiFi", "94:64:24:9e:d1:42", -63));

        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        CollectionReference collectionReference = firebaseFirestore.collection("classrooms");
        collectionReference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for(QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                        getWifiInfoList = documentSnapshot.toObject(GetWifiInfoList.class);
                    }
                }
            }
        });
    }

    private void brute_force(List<GetWifiInfo> input, GetWifiInfoList data) {



        for (GetWifiInfo getWifiInfo : data.getRSSI()) {
            Calc calc = new Calc(getWifiInfo.getBssid());
            int sum = 0;
            for (GetWifiInfo inputData : input) {
                if(inputData.getBssid().equals(getWifiInfo.getBssid())) {
                    // Bssid가 같은 값을 찾았으면, SSID 값의 차이를 구해야 한다.
                    calc.setCount(calc.getCount() + 1);
                    sum += Math.abs(inputData.getRssi() - getWifiInfo.getRssi());
                }
            }
            calc.setAvg(sum / calc.getCount());
        }
    }

    protected class ViewEx extends View
    {
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
}