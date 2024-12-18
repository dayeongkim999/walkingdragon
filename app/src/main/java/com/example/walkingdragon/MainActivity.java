package com.example.walkingdragon;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import java.util.Calendar;
import java.util.Random;
import java.util.concurrent.TimeUnit;
public class MainActivity extends AppCompatActivity {

    private TextView stepCounterBox;
    private MidnightResetWorker midnightResetWorker;
    private BroadcastReceiver stepCountReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("com.example.walkingdragon.STEP_COUNT_UPDATED".equals(intent.getAction())) {
                int stepCount = intent.getIntExtra("stepCount", 0);

                // stepCounterBox 업데이트
                stepCounterBox.setText(stepCount + " steps");
            }
        }
    };

    @Override
    protected void onStart() {
        super.onStart();

        // LocalBroadcastReceiver 등록
        IntentFilter filter = new IntentFilter("com.example.walkingdragon.STEP_COUNT_UPDATED");
        LocalBroadcastManager.getInstance(this).registerReceiver(stepCountReceiver, filter);

    }
    @Override
    protected void onStop() {
        super.onStop();

        // LocalBroadcastReceiver 해제
        LocalBroadcastManager.getInstance(this).unregisterReceiver(stepCountReceiver);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // 활동 퍼미션 체크
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_DENIED) {
            requestPermissions(new String[]{Manifest.permission.ACTIVITY_RECOGNITION}, 0);
        }

        // StepCounterService 시작
        Intent serviceIntent = new Intent(this, StepCounterService.class);
        startService(serviceIntent);

        // Initialize StepCounter UI
        stepCounterBox = findViewById(R.id.stepCounterBox);

        //workmanager 요청 생성
        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(MidnightResetWorker.class).build();

        //workmanager 실행
        WorkManager.getInstance(this).enqueue(workRequest);

        // Fragment 선택
        Fragment selectedFragment;
        selectedFragment = new ForestFragment();

        //랜덤 숫자 생성
        Random random = new Random();
        int s = 1;
        //int s = random.nextInt(4); //0~3 무작위 int 값
        // 0: Forest
        if (s == 1) {
            // 1: Beach
            selectedFragment = new BeachFragment();
        } else if (s == 2) {
            // 2: Lake
            selectedFragment = new LakeFragment();
        } else if (s == 3) {
            // 3: Mountain
            selectedFragment = new MountainFragment();
        }

        //Fragment 교체
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.backgroundFragment, selectedFragment)
                .commit();

        // 버튼 객체 가져오기
        ImageButton backButton = findViewById(R.id.backButton);

        // 버튼 클릭 리스너 추가
        backButton.setOnClickListener(v -> {
            // FlyActivity로 전환
            Intent intent = new Intent(MainActivity.this, FlyActivity.class);
            startActivity(intent); // 화면 전환 실행
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out); // 페이드 애니메이션
        });
    }
    private void scheduleMidnightResetWithWorkManager() {
        Calendar midnight = Calendar.getInstance();
        midnight.set(Calendar.HOUR_OF_DAY, 0);
        midnight.set(Calendar.MINUTE, 0);
        midnight.set(Calendar.SECOND, 0);

        if (Calendar.getInstance().after(midnight)) {
            midnight.add(Calendar.DAY_OF_YEAR, 1);
        }

        long delay = midnight.getTimeInMillis() - System.currentTimeMillis();

        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(MidnightResetWorker.class)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .build();

        WorkManager.getInstance(this).enqueue(workRequest);
    }

}