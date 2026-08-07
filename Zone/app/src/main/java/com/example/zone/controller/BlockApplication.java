package com.example.zone.controller;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import com.example.zone.model.DetectBlockedApplication;
import com.example.zone.model.BlockedAppsStore;

public class BlockApplication extends AccessibilityService {
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

        if (event.getPackageName() == null)
            return;
        if (BlockedAppsStore.isStudySessionActive(this)) {
            String packageName = event.getPackageName().toString();
            if (packageName.equals(getPackageName())) {
                return;
            }
            Log.d("APP_BLOCKER", "Opened app: " + packageName);
            if (BlockedAppsStore.isBlocked(this, packageName)) {
                Log.d("it recognizes the package", packageName);
                performGlobalAction(GLOBAL_ACTION_HOME);
                Intent intent = new Intent(
                        this,
                        DetectBlockedApplication.class
                );
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        }
    }
    @Override
    public void onInterrupt() {
    }
}


