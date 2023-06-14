package com.gachon.innergation.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.gachon.innergation.R;
import com.gachon.innergation.info.Node;

import java.util.ArrayList;
import java.util.List;

public class NavigateActivity extends AppCompatActivity {

    ImageView view1;
    TextView textView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigate);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        Intent intent = getIntent();
        ArrayList<Node> paths = (ArrayList<Node>) intent.getSerializableExtra("paths");
        textView = findViewById(R.id.textView);



        view1 = findViewById(R.id.view1);

        for (int i = 0; i < paths.size(); i++) {
            Node startPoint = paths.get(i);
            if(i+1 < paths.size()) {
                Node endPoint = paths.get(i + 1);
                drawLine(view1, startPoint.coord.y, startPoint.coord.x, endPoint.coord.y, endPoint.coord.x);
            }
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
