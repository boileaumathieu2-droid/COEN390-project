package com.example.zone.controller;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Button;

import com.example.zone.model.DetectBlockedApplication;
import com.example.zone.model.StudySessionModel;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class BlockApplication extends AccessibilityService {
    private StudySessionModel Session = StudySessionModel.getInstance();

    private final Set<String> blockedApps = new HashSet<>(Arrays.asList(
            "com.instagram.android",
            "com.google.android.youtube",
            "com.zhiliaoapp.musically",
            "com.reddit.frontpage",
            "com.facebook.katana",
            "com.snapchat.android",
            "com.google.android.gm"
    ));
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

        if (event.getPackageName() == null)
            return;
        if (Session.getStatus() == StudySessionModel.Status.ACTIVE) {
            String packageName = event.getPackageName().toString();
            Log.d("APP_BLOCKER", "Opened app: " + packageName);
            if (blockedApps.contains(packageName)) {
                Log.d("it recognizes the package", packageName);
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
    public void manageRestrictedApps(){
    }
}


