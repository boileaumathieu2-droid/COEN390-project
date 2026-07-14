package com.example.zone.model;

import android.content.Context;
import android.content.SharedPreferences;

public class Session {

    private static SharedPreferences preferences;

    public static void init(Context context) {
        preferences = context.getSharedPreferences(
                "session",
                Context.MODE_PRIVATE
        );
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
    }
}
