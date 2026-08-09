package com.example.zone.controller;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;

/** Schedules the user's daily study reminder for 7:00 PM local time. */
public final class StudyReminderScheduler {

    public static final String REMINDER_ACTION =
            "com.example.zone.action.DAILY_STUDY_REMINDER";
    private static final int REQUEST_CODE = 7300;

    private StudyReminderScheduler() {
    }

    public static void schedule(Context context) {
        AlarmManager manager = (AlarmManager)
                context.getSystemService(Context.ALARM_SERVICE);
        if (manager == null) {
            return;
        }

        Calendar nextReminder = Calendar.getInstance();
        nextReminder.set(Calendar.HOUR_OF_DAY, 19);
        nextReminder.set(Calendar.MINUTE, 0);
        nextReminder.set(Calendar.SECOND, 0);
        nextReminder.set(Calendar.MILLISECOND, 0);
        if (nextReminder.getTimeInMillis() <= System.currentTimeMillis()) {
            nextReminder.add(Calendar.DAY_OF_YEAR, 1);
        }

        manager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                nextReminder.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY,
                reminderIntent(context)
        );
    }

    public static void cancel(Context context) {
        AlarmManager manager = (AlarmManager)
                context.getSystemService(Context.ALARM_SERVICE);
        if (manager != null) {
            manager.cancel(reminderIntent(context));
        }
    }

    private static PendingIntent reminderIntent(Context context) {
        Intent intent = new Intent(context, StudyReminderReceiver.class)
                .setAction(REMINDER_ACTION);
        return PendingIntent.getBroadcast(
                context,
                REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
    }
}
