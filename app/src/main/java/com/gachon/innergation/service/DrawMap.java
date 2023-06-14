package com.gachon.innergation.service;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.widget.ImageView;

import com.gachon.innergation.R;
import com.gachon.innergation.activity.NavigateActivity;
import com.gachon.innergation.info.MapInfo;
import com.gachon.innergation.info.Node;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DrawMap {

    private static ArrayList<Node> paths;
    public static void draw(String filePath, int[][] maps, Node sourceNode, Node destNode, Context context) {
        paths = new ArrayList<>();
        MapInfo info=new MapInfo(maps,maps[0].length, maps.length,sourceNode, destNode);
        new Astar().start(info, paths);
//        Intent intent = new Intent(context, NavigateActivity.class);
//        intent.putExtra("paths", paths);
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        context.startActivity(intent);
//        Log.e("TAG", "경로 리스트 갯수" + paths.size());
//        // TODO 여기서 지도 띄워주는 액티비티 호출하면 될듯
//        printMap(filePath, maps);
    }

    public static ArrayList<Node> getPaths(){
        return paths;
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
