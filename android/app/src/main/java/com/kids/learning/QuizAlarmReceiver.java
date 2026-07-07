package com.kids.learning;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class QuizAlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        QuizLauncher.launch(context);
    }
}
