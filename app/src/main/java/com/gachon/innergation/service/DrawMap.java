package com.gachon.innergation.service;

import android.util.Log;

import androidx.annotation.NonNull;

import com.gachon.innergation.info.MapInfo;
import com.gachon.innergation.info.Node;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

public class DrawMap {
    public static void draw(String filePath, int[][] maps, Node sourceNode, Node destNode) {
        MapInfo info=new MapInfo(maps,maps[0].length, maps.length,sourceNode, destNode);
        new Astar().start(info);
        printMap(filePath, maps);
    }

    public static void printMap(String filePath, int[][] maps)
    {
        String resultPath = filePath + "/AstarMapResult.txt";
        try (PrintWriter writer = new PrintWriter(resultPath)) {
            for (int[] row : maps) {
                writer.println(Arrays.toString(row));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



}
