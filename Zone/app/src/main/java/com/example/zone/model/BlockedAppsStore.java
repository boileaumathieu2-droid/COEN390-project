package com.example.zone.model;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.view.accessibility.AccessibilityManager;

import androidx.appcompat.app.AlertDialog;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/** Stores the user's App Restrict choices for both the UI and accessibility service. */
public final class BlockedAppsStore {

    private static final String PREFERENCES_NAME = "app_restrict_preferences";
    private static final String BLOCKED_PACKAGES_KEY = "blocked_packages";
    public static Set<String> getBlockedPackages(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(
                PREFERENCES_NAME, Context.MODE_PRIVATE);

        Set<String> stored = preferences.getStringSet(BLOCKED_PACKAGES_KEY, null);

        if (stored == null) {
            return new HashSet<>();
        }

        return new HashSet<>(stored);
    }

    public static boolean isBlocked(Context context, String packageName) {
        return getBlockedPackages(context).contains(packageName);
    }

    public static void setBlocked(Context context, String packageName, boolean blocked) {
        Set<String> packages = getBlockedPackages(context);
        if (blocked) {
            packages.add(packageName);
        } else {
            packages.remove(packageName);
        }
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
                .edit()
                .putStringSet(BLOCKED_PACKAGES_KEY, packages)
                .apply();
    }

    public static void getPermission(Context context) {
        Set<String> blockedPackages = getBlockedPackages(context);
        System.out.println("BLOCKED APPS: " + blockedPackages);
        boolean x = isAccessibilityEnabled(context);
        System.out.println("ACCESSIBILITY" + x);
        if (!blockedPackages.isEmpty() && !isAccessibilityEnabled(context)) {

            getAppBlocking(context);
        }
    }
    public static boolean isAccessibilityEnabled(Context context) {
        AccessibilityManager manager = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
        return manager !=null && manager.isEnabled();
    }
    private static void getAppBlocking(Context context) {
        new AlertDialog.Builder(context)
                .setTitle("PERMISSION NOT GRANTED")
                .setMessage("Would you permit enabling app blocking?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                    context.startActivity(intent);
                })
                .setNegativeButton("No", (dialog, which) -> {
                    dialog.dismiss();
                })
                .show();
    }
    public static void clearBlockedPackages(Context context) {
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
                .edit()
                .remove(BLOCKED_PACKAGES_KEY)
                .apply();
    }
}
