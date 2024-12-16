package com.example.walkingdragon;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class StepCounterService extends Service implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor stepSensor;
    private int stepCount;

    @Override
    public void onCreate() {
        Log.e("StepCounterService", "start step counter service");

        super.onCreate();
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

        if (stepSensor != null) {
            boolean isRegistered = sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI);
            if (isRegistered) {
                Log.i("StepCounterService", "Step Counter sensor registered successfully.");
            } else {
                Log.e("StepCounterService", "Failed to register Step Counter sensor.");
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Log.i("StepCounterService", "Broadcast sensor doing");
        if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            stepCount = (int) event.values[0];
            saveStepCount(stepCount);

            // LocalBroadcast로 걸음 수 전달
            Intent intent = new Intent("com.example.walkingdragon.STEP_COUNT_UPDATED");
            intent.putExtra("stepCount", stepCount);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

            Log.i("StepCounterService", "Broadcast sent: stepCount = " + stepCount);
        }
    }

    private void saveStepCount(int stepCount) {
        SharedPreferences preferences = getSharedPreferences("StepCounterPreferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("currentStepCount", stepCount);
        editor.apply();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // 필요 시 구현
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
