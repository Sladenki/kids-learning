package com.kids.learning;

import android.app.KeyguardManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;

import com.getcapacitor.Bridge;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginHandle;
import com.kids.learning.plugins.LockTaskPlugin;
import com.kids.learning.plugins.QuizAlarmScheduler;

import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {
    public static final String EXTRA_QUIZ_DUE = "quiz_due";

    private boolean backBlocked = false;
    private boolean pendingQuizDue = false;
    private OnBackPressedCallback backCallback;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        registerPlugin(LockTaskPlugin.class);
        super.onCreate(savedInstanceState);

        backCallback =
            new OnBackPressedCallback(false) {
                @Override
                public void handleOnBackPressed() {
                    // Back blocked while in lock mode
                }
            };
        getOnBackPressedDispatcher().addCallback(this, backCallback);

        handleQuizIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleQuizIntent(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        deliverQuizDueWhenReady();
    }

    @Override
    public void onPause() {
        super.onPause();
        QuizAlarmScheduler.ensureScheduled(this);
    }

    private void handleQuizIntent(Intent intent) {
        if (intent == null || !intent.getBooleanExtra(EXTRA_QUIZ_DUE, false)) {
            return;
        }

        intent.removeExtra(EXTRA_QUIZ_DUE);
        pendingQuizDue = true;
        QuizAlarmScheduler.markQuizDue(this);
        wakeScreenForQuiz();
        deliverQuizDueWhenReady();
    }

    private void wakeScreenForQuiz() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            KeyguardManager keyguardManager = getSystemService(KeyguardManager.class);
            if (keyguardManager != null && keyguardManager.isKeyguardLocked()) {
                keyguardManager.requestDismissKeyguard(this, null);
            }
        }
    }

    private void deliverQuizDueWhenReady() {
        if (!pendingQuizDue && !QuizAlarmScheduler.isQuizDue(this)) {
            return;
        }

        Runnable attempt =
            new Runnable() {
                private int tries = 0;

                @Override
                public void run() {
                    Bridge bridge = getBridge();
                    if (bridge != null) {
                        PluginHandle handle = bridge.getPlugin("LockTask");
                        if (handle != null) {
                            Plugin plugin = handle.getInstance();
                            if (plugin instanceof LockTaskPlugin) {
                                QuizAlarmScheduler.consumeQuizDue(MainActivity.this);
                                ((LockTaskPlugin) plugin).fireQuizDue();
                                pendingQuizDue = false;
                                return;
                            }
                        }
                    }

                    tries++;
                    if (tries < 30) {
                        getWindow().getDecorView().postDelayed(this, 200);
                    }
                }
            };

        getWindow().getDecorView().post(attempt);
    }

    public void setBackBlocked(boolean blocked) {
        this.backBlocked = blocked;
        if (backCallback != null) {
            backCallback.setEnabled(blocked);
        }
    }
}
