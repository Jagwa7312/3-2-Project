package com.example.recipeapp.draw;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.recipeapp.R;
import com.divyanshu.draw.widget.DrawView;
import com.example.recipeapp.tflite.*;
import java.io.IOException;
import java.util.Locale;

public class DrawActivity extends AppCompatActivity {

    Classifier cls;
    int recognizedNumber = -1; // 판독된 숫자를 저장

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draw);

        DrawView drawView = findViewById(R.id.drawView);
        drawView.setStrokeWidth(100.0f);
        drawView.setBackgroundColor(Color.BLACK);
        drawView.setColor(Color.WHITE);

        TextView resultView = findViewById(R.id.resultView);

        //Classify 버튼 리스너
        Button classifyBtn = findViewById(R.id.btnClassify);
        classifyBtn.setOnClickListener(v -> {
            Bitmap image = drawView.getBitmap();

            Pair<Integer, Float> res = cls.classify(image);
            recognizedNumber = res.first; // 판독된 숫자 저장
            String outStr = String.format(Locale.ENGLISH, "%d, %.0f%%", res.first, res.second * 100.0f);
            resultView.setText(outStr);
        });

        cls = new Classifier(this);
        try {
            cls.init();
        } catch (IOException ioe) {
            Log.d("DigitClassifier", "failed to init Classifier", ioe);
        }

        //Clear 버튼 리스너
        Button clearBtn = findViewById(R.id.btnClear);
        clearBtn.setOnClickListener(v -> {
            recognizedNumber = -1;
            drawView.clearCanvas();
        });

        //Back 버튼 리스너
        Button backBtn = findViewById(R.id.btnBack);
        backBtn.setOnClickListener(view -> finish()); // 현재 Activity 종료, 이전 화면으로 돌아감

        //OK 버튼 리스너
        Button okBtn = findViewById(R.id.btnOK);
        okBtn.setOnClickListener(view -> {
            if (recognizedNumber == -1) {
                Toast.makeText(this, "숫자를 판독한 후 확인 버튼을 눌러주세요.", Toast.LENGTH_SHORT).show();
            } else {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("recognizedNumber", recognizedNumber); // 숫자 전달
                setResult(RESULT_OK, resultIntent);
                finish();
            }
        });
    }

    @Override
    protected void onDestroy() {
        cls.finish();
        super.onDestroy();
    }
}