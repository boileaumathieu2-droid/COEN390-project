package com.example.zone.model;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/** Stores the user's App Restrict choices for both the UI and accessibility service. */
public final class BlockedAppsStore {
    private static final String PREFERENCES_NAME = "app_restrict_preferences";
    private static final String BLOCKED_PACKAGES_KEY = "blocked_packages";

    private static final Set<String> DEFAULT_BLOCKED_PACKAGES = new HashSet<>(Arrays.asList(
            "com.instagram.android",
            "com.google.android.youtube",
            "com.zhiliaoapp.musically",
            "com.reddit.frontpage",
            "com.facebook.katana",
            "com.snapchat.android",
            "com.google.android.gm"
    ));

    private BlockedAppsStore() {
    }

    public static Set<String> getBlockedPackages(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(
                PREFERENCES_NAME, Context.MODE_PRIVATE);
        Set<String> stored = preferences.getStringSet(BLOCKED_PACKAGES_KEY, null);
        return new HashSet<>(stored == null ? DEFAULT_BLOCKED_PACKAGES : stored);
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
}
