package com.example.zone.model;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.view.accessibility.AccessibilityManager;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.NotificationManagerCompat;

import com.example.zone.R;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Stores App Restrict choices and checks Zone's own accessibility service. */
public final class BlockedAppsStore {

    private static final String PREFERENCES_NAME = "app_restrict_preferences";
    private static final String BLOCKED_PACKAGES_KEY = "blocked_packages";
    private static final String STUDY_SESSION_ACTIVE_KEY = "study_session_active";

    private BlockedAppsStore() {
    }

    public static Set<String> getBlockedPackages(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(
                PREFERENCES_NAME,
                Context.MODE_PRIVATE
        );
        Set<String> stored = preferences.getStringSet(BLOCKED_PACKAGES_KEY, null);
        return stored == null ? new HashSet<>() : new HashSet<>(stored);
    }

    public static boolean hasBlockedPackages(Context context) {
        return !getBlockedPackages(context).isEmpty();
    }

    public static boolean isBlocked(Context context, String packageName) {
        return getBlockedPackages(context).contains(packageName);
    }

    public static void setStudySessionActive(Context context, boolean active) {
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(STUDY_SESSION_ACTIVE_KEY, active)
                .apply();
    }

    public static boolean isStudySessionActive(Context context) {
        return context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
                .getBoolean(STUDY_SESSION_ACTIVE_KEY, false);
    }

    public static void setBlocked(
            Context context,
            String packageName,
            boolean blocked
    ) {
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

    /** True only when Zone's blocking service is enabled, not merely Accessibility itself. */
    public static boolean isAccessibilityEnabled(Context context) {
        AccessibilityManager manager = (AccessibilityManager)
                context.getSystemService(Context.ACCESSIBILITY_SERVICE);
        if (manager == null || !manager.isEnabled()) {
            return false;
        }

        List<AccessibilityServiceInfo> enabledServices =
                manager.getEnabledAccessibilityServiceList(
                        AccessibilityServiceInfo.FEEDBACK_ALL_MASK
                );
        for (AccessibilityServiceInfo service : enabledServices) {
            if (service.getResolveInfo() != null
                    && service.getResolveInfo().serviceInfo != null
                    && context.getPackageName().equals(
                    service.getResolveInfo().serviceInfo.packageName
            )) {
                return true;
            }
        }
        return false;
    }

    /** Prompts only when the user selected apps but Zone's service is disabled. */
    public static boolean requestPermissionIfNeeded(Activity activity) {
        if (!hasBlockedPackages(activity) || isAccessibilityEnabled(activity)) {
            return false;
        }

        showAccessibilityPrompt(activity);
        return true;
    }

    public static boolean requestPermission(Activity activity) {
        if (isAccessibilityEnabled(activity)) {
            return false;
        }

        showAccessibilityPrompt(activity);
        return true;
    }

    /** True when Android has allowed Zone to dismiss selected-app notifications. */
    public static boolean isNotificationAccessEnabled(Context context) {
        return NotificationManagerCompat.getEnabledListenerPackages(context)
                .contains(context.getPackageName());
    }

    public static boolean requestNotificationAccess(Activity activity) {
        if (isNotificationAccessEnabled(activity)) {
            return false;
        }

        new AlertDialog.Builder(activity)
                .setTitle(R.string.notification_blocking_permission_title)
                .setMessage(R.string.notification_blocking_permission_message)
                .setPositiveButton(R.string.open_settings, (dialog, which) ->
                        activity.startActivity(
                                new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                        ))
                .setNegativeButton(R.string.not_now, (dialog, which) -> dialog.dismiss())
                .show();
        return true;
    }

    private static void showAccessibilityPrompt(Activity activity) {
        new AlertDialog.Builder(activity)
                .setTitle(R.string.app_blocking_permission_title)
                .setMessage(R.string.app_blocking_permission_message)
                .setPositiveButton(R.string.open_settings, (dialog, which) ->
                        activity.startActivity(
                                new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                        ))
                .setNegativeButton(R.string.not_now, (dialog, which) -> dialog.dismiss())
                .show();
    }

    /** Backwards-compatible entry point used by older screens. */
    public static void getPermission(Context context) {
        if (context instanceof Activity) {
            requestPermission((Activity) context);
        }
    }

    public static void clearBlockedPackages(Context context) {
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
                .edit()
                .remove(BLOCKED_PACKAGES_KEY)
                .remove(STUDY_SESSION_ACTIVE_KEY)
                .apply();
    }
}
