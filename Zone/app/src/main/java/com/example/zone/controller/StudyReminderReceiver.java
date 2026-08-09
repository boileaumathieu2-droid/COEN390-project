package com.example.zone.controller;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.zone.model.NotificationPreferences;

/** Delivers the daily reminder and restores it after the phone restarts. */
public final class StudyReminderReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(
                intent == null ? null : intent.getAction()
        )) {
            if (NotificationPreferences.studyRemindersEnabled(context)) {
                StudyReminderScheduler.schedule(context);
            }
            return;
        }

        if (NotificationPreferences.studyRemindersEnabled(context)) {
            new NotificationController(context).sendNotifications(
                    "Study reminder",
                    "Your study time is ready whenever you are."
            );
        }
    }
}
