package com.example.zone.model;

import android.content.Context;
import android.content.SharedPreferences;

public class Session {

    private static final String PREFERENCES_NAME = "ZonePrefs";
    private static SharedPreferences preferences;
    private static SharedPreferences legacyPreferences;

    public static void init(Context context) {
        preferences = context.getSharedPreferences(
                PREFERENCES_NAME,
                Context.MODE_PRIVATE
        );
        legacyPreferences = context.getSharedPreferences(
                "session",
                Context.MODE_PRIVATE
        );

        // Preserve logins created by older versions that used a different file.
        if (!preferences.contains("username")
                && legacyPreferences.contains("username")) {
            preferences.edit()
                    .putString("username",
                            legacyPreferences.getString("username", null))
                    .putInt("userID",
                            legacyPreferences.getInt("userID", -1))
                    .apply();
        }
    }

    public static void setUsername(String username) {
        preferences.edit()
                .putString("username", username)
                .apply();
    }

    public static String getUsername() {
        return preferences.getString("username", null);
    }

    public static void setUserID(int id) {
        preferences.edit()
                .putInt("userID", id)
                .apply();
    }

    public static int getUserID() {
        return preferences.getInt("userID", -1);
    }

    public static void logout() {
        preferences.edit().clear().apply();
        if (legacyPreferences != null) {
            legacyPreferences.edit().clear().apply();
        }
    }
}
