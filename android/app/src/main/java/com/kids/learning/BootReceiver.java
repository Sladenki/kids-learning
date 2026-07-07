package com.kids.learning;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.kids.learning.plugins.QuizAlarmScheduler;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) {
            return;
        }

        String action = intent.getAction();
        if (!Intent.ACTION_BOOT_COMPLETED.equals(action) && !Intent.ACTION_MY_PACKAGE_REPLACED.equals(action)) {
            return;
        }

        long endTime = QuizAlarmScheduler.getQuizEndTime(context);
        if (endTime <= 0) {
            return;
        }

        long remaining = endTime - System.currentTimeMillis();
        if (remaining > 0) {
            QuizAlarmScheduler.scheduleAt(context, endTime);
            try {
                QuizTimerService.start(context, remaining);
            } catch (Exception ignored) {
                // Foreground service is optional; system alarm is enough.
            }
            return;
        }

        QuizLauncher.launch(context);
    }
}
