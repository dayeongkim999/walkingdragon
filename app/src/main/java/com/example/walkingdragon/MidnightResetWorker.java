package com.example.walkingdragon;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class MidnightResetWorker extends Worker {

    public MidnightResetWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        resetStepCountAtMidnight();
        return Result.success();
    }

    private void resetStepCountAtMidnight() {
        SharedPreferences preferences = getApplicationContext().getSharedPreferences("StepCounterPreferences", Context.MODE_PRIVATE);

        // 센서 값 가져오기
        int currentStepCount = preferences.getInt("currentStepCount", 100); // 현재 누적 걸음 수

        // 기준값으로 저장
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("stepBaseline", currentStepCount); // 센서 값 기반으로 기준값 설정
        editor.apply();

        Log.i("MidnightResetWorker", "Step count reset to baseline." +currentStepCount);
    }
}
