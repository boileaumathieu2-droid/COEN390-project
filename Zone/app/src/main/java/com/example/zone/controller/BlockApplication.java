package com.example.zone.controller;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.SystemClock;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import com.example.zone.model.BlockedAppsStore;
import com.example.zone.view.MainContainerActivity;

public class BlockApplication extends AccessibilityService {

    private static final long BLOCK_EVENT_DEBOUNCE_MS = 1_000L;
    private String lastBlockedPackage;
    private long lastBlockedAt;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null || event.getPackageName() == null) {
            return;
        }
        int eventType = event.getEventType();
        if (eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
                && eventType != AccessibilityEvent.TYPE_WINDOWS_CHANGED) {
            return;
        }
        if (!BlockedAppsStore.isStudySessionActive(this)) {
            return;
        }

        String packageName = event.getPackageName().toString();
        if (packageName.equals(getPackageName())
                || !BlockedAppsStore.isBlocked(this, packageName)) {
            return;
        }

        long now = SystemClock.elapsedRealtime();
        if (packageName.equals(lastBlockedPackage)
                && now - lastBlockedAt < BLOCK_EVENT_DEBOUNCE_MS) {
            return;
        }
        lastBlockedPackage = packageName;
        lastBlockedAt = now;

        Log.d("APP_BLOCKER", "Returning to Zone from blocked app: " + packageName);
        Intent intent = new Intent(this, MainContainerActivity.class)
                .putExtra(MainContainerActivity.EXTRA_OPEN_TAB, 1)
                .putExtra(
                        MainContainerActivity.EXTRA_BLOCKED_APP_NAME,
                        getApplicationLabel(packageName)
                )
                .addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK
                                | Intent.FLAG_ACTIVITY_CLEAR_TOP
                                | Intent.FLAG_ACTIVITY_SINGLE_TOP
                );
        startActivity(intent);
    }

    private String getApplicationLabel(String packageName) {
        PackageManager packageManager = getPackageManager();
        try {
            ApplicationInfo info = packageManager.getApplicationInfo(packageName, 0);
            CharSequence label = packageManager.getApplicationLabel(info);
            return label == null ? packageName : label.toString();
        } catch (PackageManager.NameNotFoundException ignored) {
            return packageName;
        }
    }

    @Override
    public void onInterrupt() {
    }
}


