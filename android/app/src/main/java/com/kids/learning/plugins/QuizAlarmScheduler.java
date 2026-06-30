package com.kids.learning.plugins;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.kids.learning.QuizAlarmReceiver;

public final class QuizAlarmScheduler {
    private static final int REQUEST_CODE = 1001;
    public static final String ACTION_QUIZ_DUE = "com.kids.learning.QUIZ_DUE";
    public static final String PREFS_NAME = "kids_learning";
    public static final String KEY_QUIZ_DUE = "quiz_due_pending";

    private QuizAlarmScheduler() {}

    public static void schedule(Context context, long delayMs) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            return;
        }

        Intent intent = new Intent(context, QuizAlarmReceiver.class);
        intent.setAction(ACTION_QUIZ_DUE);

        PendingIntent pending =
            PendingIntent.getBroadcast(
                context,
                REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

        long triggerAt = System.currentTimeMillis() + delayMs;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pending);
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAt, pending);
        }
    }

    public static void cancel(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            return;
        }

        Intent intent = new Intent(context, QuizAlarmReceiver.class);
        intent.setAction(ACTION_QUIZ_DUE);

        PendingIntent pending =
            PendingIntent.getBroadcast(
                context,
                REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

        alarmManager.cancel(pending);
    }

    public static void markQuizDue(Context context) {
        context
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_QUIZ_DUE, true)
            .apply();
    }

    public static boolean consumeQuizDue(Context context) {
        boolean due =
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getBoolean(KEY_QUIZ_DUE, false);
        if (due) {
            context
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(KEY_QUIZ_DUE, false)
                .apply();
        }
        return due;
    }

    public static boolean isQuizDue(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getBoolean(KEY_QUIZ_DUE, false);
    }
}
