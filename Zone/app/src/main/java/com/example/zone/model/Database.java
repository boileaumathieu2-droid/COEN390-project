package com.example.zone.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class Database extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "database.db";
    private static final int DATABASE_VERSION = 1;

    public Database(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        Log.d("DATABASE", "Helper created");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        Log.d("DATABASE", "CREATING users table");

        String userQuery =
                "CREATE TABLE users (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "username TEXT UNIQUE," +
                        "password TEXT" +
                        ")";

        db.execSQL(userQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS users");
        onCreate(db);
    }

    public boolean addUser(String username, String passwordHash) {
        if (username.length() < 6 || passwordHash.length() < 6 || !verifyUsername(username)) {
            return false;
        }
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("username", username);
        values.put("password", passwordHash);
        long result = db.insert("users", null, values);

        return result != -1L;
    }

    public boolean verifyUser(String username) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(
                "users",
                null,
                "username = ?",
                new String[]{username},
                null,
                null,
                null
        );
        boolean exists = cursor.moveToFirst();
        cursor.close();
        return exists;
    }


    public String findPassword(String username) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(
                "users",
                new String[]{"password"},
                "username = ?",
                new String[]{username},
                null,
                null,
                null
        );

        String password = null;
        if (cursor.moveToFirst()) {
            password = cursor.getString(
                    cursor.getColumnIndexOrThrow("password")
            );
        }
        cursor.close();
        return password;
    }

    public boolean verifyUsername(String username) {

        for (int i = 0; i < username.length(); i++) {
            char A = username.charAt(i);
            int ascii = (int)A;
            if(ascii < 48 || (ascii >57 && ascii <65) ||(ascii >90 && ascii <97) || ascii >122) {
                return false;
            }
        }
        return true;
    }
}

