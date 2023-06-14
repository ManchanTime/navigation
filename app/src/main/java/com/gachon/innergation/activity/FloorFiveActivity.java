package com.gachon.innergation.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.gachon.innergation.R;
import com.gachon.innergation.adapter.ChooseAdapter;

import java.util.ArrayList;

public class FloorFiveActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ChooseAdapter chooseAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<String> classrooms;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_floor_five);

        recyclerView = findViewById(R.id.recycler_classroom);
        classrooms = new ArrayList<>();
        setClassName();
        recyclerView.setHasFixedSize(true);
        layoutManager = new GridLayoutManager(this, 3);
        recyclerView.setLayoutManager(layoutManager);
        chooseAdapter = new ChooseAdapter(this, classrooms);
        chooseAdapter.setHasStableIds(true);

        recyclerView.setAdapter(chooseAdapter);

    }

    private void setClassName(){
        for(int i=1;i<10;i++){
            String name = "50" + i;
            classrooms.add(name);
        }
        for(int i=10;i<36;i++){
            String name = "5" + i;
            classrooms.add(name);
        }
        classrooms.add("5_floor_elevator_left");
        classrooms.add("5_floor_elevator_right");
        classrooms.add("artechne_5");
        classrooms.add("507_a");
    }
}