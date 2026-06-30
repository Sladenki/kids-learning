package com.kids.learning.plugins;

import android.app.Activity;
import android.os.Build;
import android.view.View;
import android.view.WindowInsetsController;

import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.kids.learning.MainActivity;

@CapacitorPlugin(name = "LockTask")
public class LockTaskPlugin extends Plugin {

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
