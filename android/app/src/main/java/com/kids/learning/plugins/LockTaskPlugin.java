package com.kids.learning.plugins;

import android.app.NotificationManager;
import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.View;
import android.view.WindowInsetsController;

import com.getcapacitor.JSObject;
import com.getcapacitor.PermissionState;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.annotation.Permission;
import com.getcapacitor.annotation.PermissionCallback;
import com.kids.learning.MainActivity;
import com.kids.learning.QuizTimerService;

@CapacitorPlugin(
    name = "LockTask",
    permissions = {
        @Permission(strings = { Manifest.permission.POST_NOTIFICATIONS }, alias = "notifications")
    }
)
public class LockTaskPlugin extends Plugin {
    private PluginCall pendingPermissionCall;

    @PluginMethod
    public void startLockTask(PluginCall call) {
        Activity activity = getActivity();
        if (activity == null) {
            call.reject("Activity not available");
            return;
        }

        activity.runOnUiThread(() -> {
            try {
                if (activity instanceof MainActivity) {
                    ((MainActivity) activity).setBackBlocked(true);
                }
                enableFullscreen(activity);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    activity.startLockTask();
                }
                call.resolve();
            } catch (Exception e) {
                call.reject("Failed to start lock task: " + e.getMessage());
            }
        });
    }

    @PluginMethod
    public void stopLockTask(PluginCall call) {
        Activity activity = getActivity();
        if (activity == null) {
            call.reject("Activity not available");
            return;
        }

        activity.runOnUiThread(() -> {
            try {
                if (activity instanceof MainActivity) {
                    ((MainActivity) activity).setBackBlocked(false);
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    activity.stopLockTask();
                }
                call.resolve();
            } catch (Exception e) {
                call.reject("Failed to stop lock task: " + e.getMessage());
            }
        });
    }

    @PluginMethod
    public void scheduleQuiz(PluginCall call) {
        long delayMs = call.getLong("delayMs", 10 * 60 * 1000L);
        Context context = getContext();

        boolean scheduled = QuizAlarmScheduler.schedule(context, delayMs);

        try {
            QuizTimerService.start(context, delayMs);
        } catch (Exception ignored) {
            // Timer notification is optional; system alarm survives app close.
        }

        JSObject result = new JSObject();
        result.put("scheduled", scheduled);
        call.resolve(result);
    }

    @PluginMethod
    public void cancelQuiz(PluginCall call) {
        Context context = getContext();
        QuizAlarmScheduler.cancel(context);
        QuizTimerService.stop(context);
        call.resolve();
    }

    @PluginMethod
    public void getPendingQuizDue(PluginCall call) {
        boolean due = QuizAlarmScheduler.consumeQuizDue(getContext());
        JSObject result = new JSObject();
        result.put("due", due);
        call.resolve(result);
    }

    @PluginMethod
    public void ensureQuizPermissions(PluginCall call) {
        Activity activity = getActivity();
        if (activity == null) {
            call.reject("Activity not available");
            return;
        }

        pendingPermissionCall = call;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (getPermissionState("notifications") != PermissionState.GRANTED) {
                requestPermissionForAlias("notifications", call, "permissionsComplete");
                return;
            }
        }

        openBatterySettingsIfNeeded(activity);
        openFullScreenIntentSettingsIfNeeded(activity);
        resolvePendingPermissionCall();
    }

    @PermissionCallback
    private void permissionsComplete(PluginCall call) {
        Activity activity = getActivity();
        if (activity != null) {
            openBatterySettingsIfNeeded(activity);
            openFullScreenIntentSettingsIfNeeded(activity);
        }
        resolvePendingPermissionCall();
    }

    private void resolvePendingPermissionCall() {
        if (pendingPermissionCall != null) {
            pendingPermissionCall.resolve();
            pendingPermissionCall = null;
        }
    }

    private void openBatterySettingsIfNeeded(Activity activity) {
        PowerManager powerManager = (PowerManager) activity.getSystemService(Context.POWER_SERVICE);
        if (powerManager == null) {
            return;
        }

        if (powerManager.isIgnoringBatteryOptimizations(activity.getPackageName())) {
            return;
        }

        try {
            Intent intent =
                new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            intent.setData(Uri.parse("package:" + activity.getPackageName()));
            activity.startActivity(intent);
        } catch (Exception ignored) {
            try {
                activity.startActivity(new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS));
            } catch (Exception ignoredAgain) {
                // Best effort only.
            }
        }
    }

    private void openFullScreenIntentSettingsIfNeeded(Activity activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            return;
        }

        NotificationManager manager = activity.getSystemService(NotificationManager.class);
        if (manager == null || manager.canUseFullScreenIntent()) {
            return;
        }

        try {
            Intent intent = new Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT);
            intent.setData(Uri.parse("package:" + activity.getPackageName()));
            activity.startActivity(intent);
        } catch (Exception ignored) {
            // Best effort only.
        }
    }

    public void fireQuizDue() {
        notifyListeners("quizDue", new JSObject());
    }

    private void enableFullscreen(Activity activity) {
        View decorView = activity.getWindow().getDecorView();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            activity.getWindow().setDecorFitsSystemWindows(false);
            WindowInsetsController controller = decorView.getWindowInsetsController();
            if (controller != null) {
                controller.hide(
                    android.view.WindowInsets.Type.statusBars()
                        | android.view.WindowInsets.Type.navigationBars()
                );
                controller.setSystemBarsBehavior(
                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                );
            }
        } else {
            decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            );
        }
    }
}
