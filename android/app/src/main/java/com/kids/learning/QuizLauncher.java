package com.kids.learning;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.kids.learning.plugins.QuizAlarmScheduler;

public final class QuizLauncher {
    private static final String CHANNEL_ID = "quiz_alert";
    private static final int NOTIFICATION_ID = 2001;

    private QuizLauncher() {}

    public static void launch(Context context) {
        QuizAlarmScheduler.markQuizDue(context);

        Intent activityIntent = new Intent(context, MainActivity.class);
        activityIntent.setFlags(
            Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP
        );
        activityIntent.putExtra(MainActivity.EXTRA_QUIZ_DUE, true);

        PendingIntent fullScreenPending =
            PendingIntent.getActivity(
                context,
                2001,
                activityIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

        createChannel(context);

        NotificationCompat.Builder builder =
            new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_quiz_notify)
                .setContentTitle("Время учиться!")
                .setContentText("Ответь на 2 вопроса")
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setAutoCancel(true)
                .setContentIntent(fullScreenPending)
                .setFullScreenIntent(fullScreenPending, true);

        NotificationManager manager = context.getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.notify(NOTIFICATION_ID, builder.build());
        }

        context.startActivity(activityIntent);
    }

    private static void createChannel(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }

        NotificationChannel channel =
            new NotificationChannel(
                CHANNEL_ID,
                "Вопросы",
                NotificationManager.IMPORTANCE_HIGH
            );
        channel.setDescription("Открывает приложение, когда пора отвечать на вопросы");
        channel.setLockscreenVisibility(NotificationCompat.VISIBILITY_PUBLIC);

        NotificationManager manager = context.getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.createNotificationChannel(channel);
        }
    }
}
