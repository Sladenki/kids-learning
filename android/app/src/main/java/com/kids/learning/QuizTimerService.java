package com.kids.learning;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import androidx.core.app.NotificationCompat;

import com.kids.learning.plugins.QuizAlarmScheduler;

public class QuizTimerService extends Service {
    public static final String ACTION_START = "com.kids.learning.action.START_TIMER";
    public static final String ACTION_STOP = "com.kids.learning.action.STOP_TIMER";
    public static final String EXTRA_END_TIME = "end_time_ms";

    private static final int NOTIFICATION_ID = 1001;
    private static final String CHANNEL_ID = "quiz_timer";

    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable tickRunnable;

    public static void start(Context context, long delayMs) {
        long endTime = System.currentTimeMillis() + delayMs;
        QuizAlarmScheduler.saveQuizEndTime(context, endTime);

        Intent intent = new Intent(context, QuizTimerService.class);
        intent.setAction(ACTION_START);
        intent.putExtra(EXTRA_END_TIME, endTime);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }

    public static void stop(Context context) {
        Intent intent = new Intent(context, QuizTimerService.class);
        intent.setAction(ACTION_STOP);
        context.startService(intent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            long savedEnd = QuizAlarmScheduler.getQuizEndTime(this);
            if (savedEnd > System.currentTimeMillis()) {
                beginCountdown(savedEnd);
            } else {
                stopSelf();
            }
            return START_STICKY;
        }

        String action = intent.getAction();
        if (ACTION_STOP.equals(action)) {
            stopCountdown();
            stopForeground(true);
            stopSelf();
            return START_NOT_STICKY;
        }

        long endTime = intent.getLongExtra(EXTRA_END_TIME, QuizAlarmScheduler.getQuizEndTime(this));
        if (endTime <= System.currentTimeMillis()) {
            QuizLauncher.launch(this);
            stopSelf();
            return START_NOT_STICKY;
        }

        beginCountdown(endTime);
        return START_STICKY;
    }

    private void beginCountdown(long endTimeMs) {
        createChannel();
        startForeground(NOTIFICATION_ID, buildNotification(getRemainingLabel(endTimeMs)));

        if (tickRunnable != null) {
            handler.removeCallbacks(tickRunnable);
        }

        tickRunnable =
            new Runnable() {
                @Override
                public void run() {
                    long remaining = endTimeMs - System.currentTimeMillis();
                    if (remaining <= 0) {
                        QuizLauncher.launch(QuizTimerService.this);
                        QuizAlarmScheduler.clearQuizEndTime(QuizTimerService.this);
                        stopForeground(true);
                        stopSelf();
                        return;
                    }

                    NotificationManager manager = getSystemService(NotificationManager.class);
                    if (manager != null) {
                        manager.notify(NOTIFICATION_ID, buildNotification(getRemainingLabel(endTimeMs)));
                    }

                    handler.postDelayed(this, 1000);
                }
            };

        handler.post(tickRunnable);
    }

    private void stopCountdown() {
        if (tickRunnable != null) {
            handler.removeCallbacks(tickRunnable);
            tickRunnable = null;
        }
        QuizAlarmScheduler.clearQuizEndTime(this);
    }

    private String getRemainingLabel(long endTimeMs) {
        long remainingSec = Math.max(0, (endTimeMs - System.currentTimeMillis() + 999) / 1000);
        long minutes = remainingSec / 60;
        long seconds = remainingSec % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }

        NotificationChannel channel =
            new NotificationChannel(
                CHANNEL_ID,
                "Таймер обучения",
                NotificationManager.IMPORTANCE_LOW
            );
        channel.setDescription("Показывает, когда откроется следующий вопрос");
        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.createNotificationChannel(channel);
        }
    }

    private Notification buildNotification(String timeLeft) {
        Intent openIntent = new Intent(this, MainActivity.class);
        PendingIntent contentIntent =
            PendingIntent.getActivity(
                this,
                3001,
                openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

        return new NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_quiz_notify)
            .setContentTitle("Учимся вместе")
            .setContentText("Следующий вопрос через " + timeLeft)
            .setContentIntent(contentIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build();
    }

    @Override
    public void onDestroy() {
        if (tickRunnable != null) {
            handler.removeCallbacks(tickRunnable);
        }
        super.onDestroy();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        QuizAlarmScheduler.ensureScheduled(this);
    }
}
