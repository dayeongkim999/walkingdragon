package com.example.walkingdragon;

import static androidx.fragment.app.FragmentManager.TAG;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.widget.Toast;

public class StepCounter implements SensorEventListener {
    private SensorManager sensorManager;
    private Sensor stepSensor;
    private int stepCount = 0;
    private OnStepCountChangeListener listener;
    private static final String PREF_NAME = "StepCounterPreferences";

    private SharedPreferences preferences;


    public interface OnStepCountChangeListener {
        void onStepCountChanged(int steps);
    }

    public void setOnStepCountChangeListener(OnStepCountChangeListener listener) {
        this.listener = listener;
    }

    public StepCounter(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
            if (stepSensor == null) {
                Log.e("StepCounter", "Step Counter sensor not available!");
            } else {
                Log.i("StepCounter", "Step Counter sensor initialized successfully.");
            }
        }
        else{
            Toast.makeText(context, "No step Sensor", Toast.LENGTH_SHORT).show();
        }
    }

    public void start() {
        if (stepSensor != null) {
            //SENSOR_DELAY_UI: 6000초 딜레이
            boolean isRegistered = sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI);
            if (isRegistered) {
                Log.i("StepCounter", "Step Counter sensor registered successfully.");
            } else {
                Log.e("StepCounter", "Failed to register Step Counter sensor.");
            }
        }
        else {
            Log.e("StepCounter", "Step Counter sensor is null.");
        }
    }

    public void stop() {
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        //걸음 센서 이벤트 발생시
        if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            Log.println(Log.INFO, "onSensorChanged: ", String.valueOf(stepCount));
            stepCount = (int) event.values[0];
            if (listener != null) {
                listener.onStepCountChanged(stepCount);
            }
        }
    }

    //자정 초기화용: 걸음 기준값 저장하기
    public void saveStepCountBaseline(int currentStepCount) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("stepBaseline", currentStepCount);
        editor.putLong("lastResetTime", System.currentTimeMillis());
        editor.apply();
    }

    public int getStepCountBaseline() {
        return preferences.getInt("stepBaseline", 0);
    }

    //당일 걸음 수 계산
    public int getTodayStepCount(int currentStepCount) {
        return currentStepCount - getStepCountBaseline();
    }

    //자정 기준값 갱신
    private void resetStepCountAtMidnight() {
        // 현재 걸음 수 저장
        int currentStepCount = stepCount;
        saveStepCountBaseline(currentStepCount);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // 필요 시 구현
    }
}
