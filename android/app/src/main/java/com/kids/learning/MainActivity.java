package com.kids.learning;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;

import com.getcapacitor.BridgeActivity;
import com.kids.learning.plugins.LockTaskPlugin;

public class MainActivity extends BridgeActivity {
    private boolean backBlocked = false;
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
    }

    public void setBackBlocked(boolean blocked) {
        this.backBlocked = blocked;
        if (backCallback != null) {
            backCallback.setEnabled(blocked);
        }
    }
}
