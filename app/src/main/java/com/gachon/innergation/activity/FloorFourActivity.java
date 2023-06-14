package com.gachon.innergation.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.gachon.innergation.R;
import com.gachon.innergation.adapter.ChooseAdapter;
import com.gachon.innergation.adapter.ClassAdapter;

import java.util.ArrayList;

public class FloorFourActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ChooseAdapter chooseAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<String> classrooms;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_floor_four);

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
            String name = "40" + i;
            classrooms.add(name);
        }
        for(int i=10;i<37;i++){
            String name = "4" + i;
            classrooms.add(name);
        }
        classrooms.add("4_floor_elevator_left");
        classrooms.add("4_floor_elevator_right");
        classrooms.add("4_floor_terrace");
        classrooms.add("artechne_4");
        classrooms.add("407_a");
    }
}