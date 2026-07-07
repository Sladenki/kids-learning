package com.kids.learning.plugins;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.kids.learning.MainActivity;
import com.kids.learning.QuizAlarmReceiver;

public final class QuizAlarmScheduler {
    private static final String TAG = "QuizAlarmScheduler";
    private static final int REQUEST_CODE = 1001;
    public static final String ACTION_QUIZ_DUE = "com.kids.learning.QUIZ_DUE";
    public static final String PREFS_NAME = "kids_learning";
    public static final String KEY_QUIZ_DUE = "quiz_due_pending";
    public static final String KEY_QUIZ_END_TIME = "quiz_end_time_ms";

    private QuizAlarmScheduler() {}

    public static boolean schedule(Context context, long delayMs) {
        return scheduleAt(context, System.currentTimeMillis() + delayMs);
    }

    public static boolean scheduleAt(Context context, long triggerAt) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            return false;
        }

        saveQuizEndTime(context, triggerAt);

        Intent intent = new Intent(context, QuizAlarmReceiver.class);
        intent.setAction(ACTION_QUIZ_DUE);

        PendingIntent pending =
            PendingIntent.getBroadcast(
                context,
                REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

        Intent showIntent = new Intent(context, MainActivity.class);
        showIntent.putExtra(MainActivity.EXTRA_QUIZ_DUE, true);
        PendingIntent showPending =
            PendingIntent.getActivity(
                context,
                1002,
                showIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                AlarmManager.AlarmClockInfo alarmClockInfo =
                    new AlarmManager.AlarmClockInfo(triggerAt, showPending);
                alarmManager.setAlarmClock(alarmClockInfo, pending);
                Log.i(TAG, "Alarm scheduled at " + triggerAt);
                return true;
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pending);
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAt, pending);
            }
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Failed to schedule alarm", e);
            return false;
        }
    }

    public static void ensureScheduled(Context context) {
        long endTime = getQuizEndTime(context);
        if (endTime <= System.currentTimeMillis()) {
            return;
        }
        scheduleAt(context, endTime);
    }

    public static void cancel(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
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

        clearQuizEndTime(context);
    }

    public static void saveQuizEndTime(Context context, long endTimeMs) {
        context
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putLong(KEY_QUIZ_END_TIME, endTimeMs)
            .apply();
    }

    public static long getQuizEndTime(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getLong(KEY_QUIZ_END_TIME, 0);
    }

    public static void clearQuizEndTime(Context context) {
        context
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .remove(KEY_QUIZ_END_TIME)
            .apply();
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
