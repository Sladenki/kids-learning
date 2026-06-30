package com.kids.learning;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.kids.learning.plugins.QuizAlarmScheduler;

public class QuizAlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        QuizAlarmScheduler.markQuizDue(context);

        Intent launch = new Intent(context, MainActivity.class);
        launch.setFlags(
            Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP
        );
        launch.putExtra(MainActivity.EXTRA_QUIZ_DUE, true);
        context.startActivity(launch);
    }
}
