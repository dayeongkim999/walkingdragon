package com.example.walkingdragon;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private TextView stepCounterBox;
    private StepCounter stepCounter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // 활동 퍼미션 체크
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_DENIED){
            requestPermissions(new String[]{Manifest.permission.ACTIVITY_RECOGNITION}, 0);
        }

        // Initialize StepCounter UI
        stepCounterBox = findViewById(R.id.stepCounterBox);

        // Fragment 선택
        Fragment selectedFragment;
        selectedFragment = new ForestFragment();

        //랜덤 숫자 생성
        Random random = new Random();
        int s = random.nextInt(4); //0~3 무작위 int 값
        // 0: Forest
        if(s==1) {
            // 1: Beach
            selectedFragment = new BeachFragment();
        } else if(s==2){
            // 2: Lake
            selectedFragment = new LakeFragment();
        } else if(s==3){
            // 3: Mountain
            selectedFragment = new MountainFragment();
        }

        //Fragment 교체
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.backgroundFragment, selectedFragment)
                .commit();

        // Initialize and Start StepCounter
        stepCounter = new StepCounter(this);
        stepCounter.start();
        stepCounter.setOnStepCountChangeListener(new StepCounter.OnStepCountChangeListener() {
            @Override
            public void onStepCountChanged(int steps) {
                stepCounterBox.setText(steps + " steps");
            }
        });

    }


}