package com.example.walkingdragon;

import android.content.Context;

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
        // 자정 기준값 갱신
        resetStepCountAtMidnight();
        return Result.success();
    }
}
