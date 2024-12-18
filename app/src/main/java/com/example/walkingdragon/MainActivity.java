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
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import java.util.Calendar;
import java.util.List;
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

        // 초기 프래그먼트 설정
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.backgroundFragment, new ForestFragment())
                    .commit();
        }

        // 프래그먼트가 추가된 후 실행
        getSupportFragmentManager().executePendingTransactions();

        // 현재 프래그먼트 확인
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.backgroundFragment);
        if (currentFragment != null) {
            Log.d("CurrentFragment", "Found fragment: " + currentFragment.getClass().getSimpleName());
        } else {
            Log.e("CurrentFragment", "No fragment found.");
        }

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
        int s = random.nextInt(4); //0~3 무작위 int 값
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


        // Intent에서 비행 시간 가져오기
        long flightDuration = getIntent().getLongExtra("FLIGHT_DURATION", 0);
// 비행 시간(ms) 로그 출력
        Log.d("FlightDuration in MainActivity", "Duration (ms): " + flightDuration);
        if (flightDuration > 0) {
            hideDragonAndStartCountdown(flightDuration); // 드래곤 숨기기 및 카운트다운 시작
        }
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


    /**
     * 드래곤 숨기고 카운트다운 시작
     */
    private void hideDragonAndStartCountdown(long duration) {
        // 현재 표시 중인 프래그먼트 가져오기
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.backgroundFragment);

        if (currentFragment != null && currentFragment.getView() != null) {
            // 현재 프래그먼트의 뷰에서 드래곤 이미지 찾기
            ImageView dragonView = currentFragment.getView().findViewById(R.id.dragonAnimation);
            if (dragonView != null) {
                // 드래곤 숨기기
                dragonView.setVisibility(View.INVISIBLE);

                // 남은 시간 표시
                TextView countdownTextView = findViewById(R.id.countdownTextView);
                countdownTextView.setVisibility(View.VISIBLE);

                // 카운트다운 시작
                new CountDownTimer(duration, 1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        int seconds = (int) (millisUntilFinished / 1000);
                        int minutes = seconds / 60;
                        seconds = seconds % 60;
                        countdownTextView.setText(String.format("남은 시간: %02d:%02d", minutes, seconds));
                    }

                    @Override
                    public void onFinish() {
                        // 드래곤 다시 표시
                        dragonView.setVisibility(View.VISIBLE);
                        countdownTextView.setVisibility(View.GONE);
                    }
                }.start();
            } else {
                Log.e("HideDragon", "Dragon view not found in the current fragment.");
            }
        } else {
            Log.e("HideDragon", "No active fragment found or it has no view.");
        }
    }

}