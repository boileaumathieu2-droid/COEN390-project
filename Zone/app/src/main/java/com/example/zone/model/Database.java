package com.example.zone.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

public class Database extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "database.db";
    private static final int DATABASE_VERSION = 2;

    public Database(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        Log.d("DATABASE", "Helper created");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        Log.d("DATABASE", "CREATING tables");



        String userQuery =
                "CREATE TABLE users (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "username TEXT UNIQUE," +
                        "password TEXT" +
                        ")";

        String subjectQuery =
                "CREATE TABLE subjects (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "user_id INTEGER," +
                        "subject_name TEXT," +
                        "FOREIGN KEY(user_id) REFERENCES users(id)" +
                        ")";

        String gradeQuery =
                "CREATE TABLE grades (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "subject_id INTEGER," +
                        "grade TEXT," +
                        "FOREIGN KEY(subject_id) REFERENCES subjects(id)" +
                        ")";

        String sessionQuery =
                "CREATE TABLE sessions (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "user_id INTEGER," +
                        "start_time TEXT," +
                        "end_time TEXT," +
                        "duration INTEGER," +
                        "status TEXT," +
                        "objective TEXT," +
                        "objective_met INTEGER," +
                        "productivity_rating INTEGER," +
                        "resting_heart_rate INTEGER," +
                        "avg_heart_rate INTEGER," +
                        "max_heart_rate INTEGER," +
                        "min_heart_rate INTEGER," +
                        "heart_rate_data TEXT," +
                        "FOREIGN KEY(user_id) REFERENCES users(id)" +
                        ")";

        db.execSQL(userQuery);
        db.execSQL(subjectQuery);
        db.execSQL(gradeQuery);
        db.execSQL(sessionQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            String sessionQuery =
                    "CREATE TABLE sessions (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "user_id INTEGER," +
                            "start_time TEXT," +
                            "end_time TEXT," +
                            "duration INTEGER," +
                            "status TEXT," +
                            "objective TEXT," +
                            "objective_met INTEGER," +
                            "productivity_rating INTEGER," +
                            "resting_heart_rate INTEGER," +
                            "avg_heart_rate INTEGER," +
                            "max_heart_rate INTEGER," +
                            "min_heart_rate INTEGER," +
                            "heart_rate_data TEXT," +
                            "FOREIGN KEY(user_id) REFERENCES users(id)" +
                            ")";
            db.execSQL(sessionQuery);
        }
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

    public int getUserID(String username) {

        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(
                "users",
                new String[]{"id"},
                "username=?",
                new String[]{username},
                null,
                null,
                null
        );

        int userID = -1;

        if(cursor.moveToFirst()) {
            userID = cursor.getInt(
                    cursor.getColumnIndexOrThrow("id")
            );
        }

        cursor.close();

        return userID;
    }

    public long addSubject(int userID, String subjectName){

        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("user_id", userID);
        values.put("subject_name", subjectName);

        return db.insert("subjects", null, values);
    }

    public ArrayList<Subject> getSubjects(int userID){

        ArrayList<Subject> subjects = new ArrayList<>();

        SQLiteDatabase db = getReadableDatabase();


        Cursor cursor = db.query(
                "subjects",
                null,
                "user_id=?",
                new String[]{String.valueOf(userID)},
                null,
                null,
                null
        );


        while(cursor.moveToNext()){

            int subjectID = cursor.getInt(
                    cursor.getColumnIndexOrThrow("id")
            );

            String name = cursor.getString(
                    cursor.getColumnIndexOrThrow("subject_name")
            );


            ArrayList<String> grades = getGrades(subjectID);


            subjects.add(new Subject(name, grades));
        }


        cursor.close();

        return subjects;
    }

    public ArrayList<String> getGrades(int subjectID){

        ArrayList<String> grades = new ArrayList<>();

        SQLiteDatabase db = getReadableDatabase();


        Cursor cursor = db.query(
                "grades",
                null,
                "subject_id=?",
                new String[]{String.valueOf(subjectID)},
                null,
                null,
                null
        );


        while(cursor.moveToNext()){

            grades.add(
                    cursor.getString(
                            cursor.getColumnIndexOrThrow("grade")
                    )
            );
        }


        cursor.close();

        return grades;
    }

    public boolean addGrade(long subjectID, String grade){

        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("subject_id", subjectID);
        values.put("grade", grade);

        return db.insert("grades", null, values) != -1;
    }

    public long addSession(int userID, StudySessionModel session) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("user_id", userID);
        values.put("start_time", session.getStartTime().toString());
        values.put("end_time", session.getEndTime() != null ? session.getEndTime().toString() : null);
        values.put("duration", session.getDuration());
        values.put("status", session.getStatus().name());
        values.put("objective", session.getObjective());
        values.put("objective_met", session.getObjectiveMet() ? 1 : 0);
        values.put("productivity_rating", session.getProductivityRating());
        values.put("resting_heart_rate", session.getRestingHeartRate());
        values.put("avg_heart_rate", session.getHeartRate());
        values.put("max_heart_rate", session.getMaxHeartRate());
        values.put("min_heart_rate", session.getMinHeartRate());

        // Serialize int[] to CSV
        StringBuilder sb = new StringBuilder();
        int[] data = session.getHeartRateData();
        if (data != null) {
            for (int i = 0; i < data.length; i++) {
                sb.append(data[i]);
                if (i < data.length - 1) sb.append(",");
            }
        }
        values.put("heart_rate_data", sb.toString());

        return db.insert("sessions", null, values);
    }
}


