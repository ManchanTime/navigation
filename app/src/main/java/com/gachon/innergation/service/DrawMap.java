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

    private static ArrayList<Node> paths = new ArrayList<>();
    public static void draw(String filePath, int[][] maps, Node sourceNode, Node destNode, Context context) {
        MapInfo info=new MapInfo(maps,maps[0].length, maps.length,sourceNode, destNode);
        new Astar().start(info, paths);
        Intent intent = new Intent(context, NavigateActivity.class);
        intent.putExtra("paths", paths);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
//        Log.e("TAG", "경로 리스트 갯수" + paths.size());
//        // TODO 여기서 지도 띄워주는 액티비티 호출하면 될듯
//        printMap(filePath, maps);
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

    public void drawLine(ImageView imageView, float startX, float startY, float endX, float endY) {
        Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        Bitmap mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);

        Canvas canvas = new Canvas(mutableBitmap);
        Paint paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(7.0f);

        // 상대적인 위치를 기준으로 선을 그립니다.
        float startXPos = (startX / 100) * canvas.getWidth();
        float startYPos = (startY / 100) * canvas.getHeight();
        float endXPos = (endX / 100) * canvas.getWidth();
        float endYPos = (endY / 100) * canvas.getHeight();

        canvas.drawLine(startXPos, startYPos, endXPos, endYPos, paint);

        imageView.setImageBitmap(mutableBitmap);
    }



}
