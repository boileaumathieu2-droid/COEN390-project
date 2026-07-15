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
    private static final int DATABASE_VERSION = 1;

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

        db.execSQL(userQuery);
        db.execSQL(subjectQuery);
        db.execSQL(gradeQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS users");
        db.execSQL("DROP TABLE IF EXISTS subjects");
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

    public boolean subjectAlreadyExists(int userID, String subjectName) {

        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(
                "subjects",
                null,
                "user_id=? AND subject_name=?",
                new String[]{
                        String.valueOf(userID),
                        subjectName
                },
                null,
                null,
                null
        );

        boolean exists = cursor.moveToFirst();
        cursor.close();

        return exists;
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


            subjects.add(new Subject(subjectID, name, grades));
        }


        cursor.close();

        return subjects;
    }

    public boolean deleteSubject(int subjectID){
        SQLiteDatabase db = getWritableDatabase();
        db.delete(
                "grades", "subject_id=?", new String[]{String.valueOf(subjectID)});
        int deleted = db.delete(
                "subjects",
                "id=?",
                new String[]{String.valueOf(subjectID)}
        );
        return deleted == 1;
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
}



