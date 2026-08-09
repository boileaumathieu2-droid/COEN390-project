package com.example.zone.model;

import android.content.Context;
import android.content.SharedPreferences;

/** One place for all notification-setting keys and their old-key migration. */
public final class NotificationPreferences {

    private static final String FILE_NAME = "settings";
    private static final String STUDY_REMINDERS = "study_reminders";
    private static final String SESSION_COMPLETE = "session_complete_notifications";
    private static final String BREAK_REMINDERS = "break_reminders";
    private static final String MUTE_DURING_STUDY = "Mute";

    private NotificationPreferences() {
    }

    private static SharedPreferences preferences(Context context) {
        return context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
    }

    private static boolean readWithOldKey(
            Context context,
            String key,
            String oldKey
    ) {
        SharedPreferences preferences = preferences(context);
        return preferences.contains(key)
                ? preferences.getBoolean(key, false)
                : preferences.getBoolean(oldKey, false);
    }

    public static boolean studyRemindersEnabled(Context context) {
        return readWithOldKey(context, STUDY_REMINDERS, "Notifications");
    }

    public static void setStudyRemindersEnabled(Context context, boolean enabled) {
        preferences(context).edit().putBoolean(STUDY_REMINDERS, enabled).apply();
    }

    public static boolean sessionCompleteEnabled(Context context) {
        return readWithOldKey(context, SESSION_COMPLETE, "temporary");
    }

    public static void setSessionCompleteEnabled(Context context, boolean enabled) {
        preferences(context).edit().putBoolean(SESSION_COMPLETE, enabled).apply();
    }

    public static boolean breakRemindersEnabled(Context context) {
        return readWithOldKey(context, BREAK_REMINDERS, "alsoTemp");
    }

    public static void setBreakRemindersEnabled(Context context, boolean enabled) {
        preferences(context).edit().putBoolean(BREAK_REMINDERS, enabled).apply();
    }

    public static boolean muteDuringStudyEnabled(Context context) {
        return preferences(context).getBoolean(MUTE_DURING_STUDY, false);
    }

    public static void setMuteDuringStudyEnabled(Context context, boolean enabled) {
        preferences(context).edit().putBoolean(MUTE_DURING_STUDY, enabled).apply();
    }
}
