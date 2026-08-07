package com.example.zone.controller;

import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

import com.example.zone.model.BlockedAppsStore;

/** Removes notifications from selected apps while a study session is active. */
public class RestrictedNotificationListener extends NotificationListenerService {

    private static volatile RestrictedNotificationListener activeInstance;

    @Override
    public void onListenerConnected() {
        super.onListenerConnected();
        activeInstance = this;
        cancelBlockedNotifications();
    }

    @Override
    public void onListenerDisconnected() {
        if (activeInstance == this) {
            activeInstance = null;
        }
        super.onListenerDisconnected();
    }

    @Override
    public void onDestroy() {
        if (activeInstance == this) {
            activeInstance = null;
        }
        super.onDestroy();
    }

    @Override
    public void onNotificationPosted(StatusBarNotification notification) {
        if (notification == null || !shouldCancel(notification.getPackageName())) {
            return;
        }
        cancelNotification(notification.getKey());
    }

    private boolean shouldCancel(String packageName) {
        return packageName != null
                && BlockedAppsStore.isStudySessionActive(this)
                && BlockedAppsStore.isBlocked(this, packageName);
    }

    private void cancelBlockedNotifications() {
        if (!BlockedAppsStore.isStudySessionActive(this)) {
            return;
        }
        StatusBarNotification[] notifications = getActiveNotifications();
        if (notifications == null) {
            return;
        }
        for (StatusBarNotification notification : notifications) {
            if (notification != null && shouldCancel(notification.getPackageName())) {
                cancelNotification(notification.getKey());
            }
        }
    }

    public static void cancelBlockedNotificationsIfConnected() {
        RestrictedNotificationListener listener = activeInstance;
        if (listener != null) {
            listener.cancelBlockedNotifications();
        }
    }
}
