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
    private static final int DATABASE_VERSION = 3;

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

        String objectiveQuery =
                "CREATE TABLE objectives (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "user_id INTEGER," +
                        "objective_text TEXT," +
                        "objective_date TEXT," +
                        "event_name TEXT," +
                        "completion_time TEXT," +
                        "task_type TEXT," +
                        "FOREIGN KEY(user_id) REFERENCES users(id)" +
                        ")";

        db.execSQL(userQuery);
        db.execSQL(subjectQuery);
        db.execSQL(gradeQuery);
        db.execSQL(sessionQuery);
        db.execSQL(objectiveQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("DROP TABLE IF EXISTS users");
            db.execSQL("DROP TABLE IF EXISTS subjects");
            db.execSQL("DROP TABLE IF EXISTS grades");
            db.execSQL("DROP TABLE IF EXISTS objectives");
            db.execSQL("DROP TABLE IF EXISTS sessions");
            onCreate(db);
            return;
        }
        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE objectives ADD COLUMN event_name TEXT NOT NULL DEFAULT ''");
            db.execSQL("ALTER TABLE objectives ADD COLUMN completion_time TEXT NOT NULL DEFAULT ''");
            db.execSQL("ALTER TABLE objectives ADD COLUMN task_type TEXT NOT NULL DEFAULT 'Other'");
            db.execSQL("UPDATE objectives SET event_name=objective_text WHERE event_name='' ");
        }
    }

    public boolean addUser(String username, String passwordHash) {
        if (username == null || passwordHash == null
                || username.length() < 6 || passwordHash.length() < 6
                || !verifyUsername(username)) {
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


            subjects.add(new Subject(subjectID, name, grades));
        }


        cursor.close();

        return subjects;
    }

    public boolean subjectAlreadyExists(int userID, String subjectName) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(
                "subjects",
                new String[]{"id"},
                "user_id=? AND subject_name=? COLLATE NOCASE",
                new String[]{String.valueOf(userID), subjectName.trim()},
                null,
                null,
                null,
                "1"
        );
        boolean exists = cursor.moveToFirst();
        cursor.close();
        return exists;
    }

    public boolean deleteSubject(int subjectID) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete("grades", "subject_id=?", new String[]{String.valueOf(subjectID)});
            int deleted = db.delete(
                    "subjects", "id=?", new String[]{String.valueOf(subjectID)});
            db.setTransactionSuccessful();
            return deleted == 1;
        } finally {
            db.endTransaction();
        }
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

    public ArrayList<StudySessionModel> getAllSessions(int userID) {
        ArrayList<StudySessionModel> sessions = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(
                "sessions",
                null,
                "user_id=?",
                new String[]{String.valueOf(userID)},
                null,
                null,
                "id DESC"
        );

        while (cursor.moveToNext()) {
            StudySessionModel session = new StudySessionModel();
            session.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
            
            String startTimeStr = cursor.getString(cursor.getColumnIndexOrThrow("start_time"));
            String endTimeStr = cursor.getString(cursor.getColumnIndexOrThrow("end_time"));
            
            session.setStartTime(java.time.LocalDateTime.parse(startTimeStr));
            if (endTimeStr != null) {
                session.setEndTime(java.time.LocalDateTime.parse(endTimeStr));
            }
            
            session.setDuration(cursor.getInt(cursor.getColumnIndexOrThrow("duration")));
            session.setStatus(StudySessionModel.Status.valueOf(cursor.getString(cursor.getColumnIndexOrThrow("status"))));
            session.setObjectiveMet(cursor.getInt(cursor.getColumnIndexOrThrow("objective_met")) == 1);
            session.setProductivityRating(cursor.getInt(cursor.getColumnIndexOrThrow("productivity_rating")));
            session.setRestingHeartRate(cursor.getInt(cursor.getColumnIndexOrThrow("resting_heart_rate")));
            session.setHeartRate(cursor.getInt(cursor.getColumnIndexOrThrow("avg_heart_rate")));
            session.setMaxHeartRate(cursor.getInt(cursor.getColumnIndexOrThrow("max_heart_rate")));
            session.setMinHeartRate(cursor.getInt(cursor.getColumnIndexOrThrow("min_heart_rate")));
            
            String csv = cursor.getString(cursor.getColumnIndexOrThrow("heart_rate_data"));
            if (csv != null && !csv.isEmpty()) {
                String[] parts = csv.split(",");
                for (String part : parts) {
                    try {
                        int hr = Integer.parseInt(part);
                        // We need a way to add to heartRateDataList in StudySessionModel
                        // I'll add a helper method to StudySessionModel for this
                        session.addHistoricalHeartRate(hr);
                    } catch (NumberFormatException ignored) {}
                }
            }
            sessions.add(session);
        }
        cursor.close();
        return sessions;
    }

    public int[] getLastSessionHeartRateData(int userID) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(
                "sessions",
                new String[]{"heart_rate_data"},
                "user_id=?",
                new String[]{String.valueOf(userID)},
                null,
                null,
                "id DESC",
                "1"
        );

        int[] data = null;
        if (cursor.moveToFirst()) {
            String csv = cursor.getString(cursor.getColumnIndexOrThrow("heart_rate_data"));
            if (csv != null && !csv.isEmpty()) {
                String[] parts = csv.split(",");
                data = new int[parts.length];
                for (int i = 0; i < parts.length; i++) {
                    try {
                        data[i] = Integer.parseInt(parts[i]);
                    } catch (NumberFormatException e) {
                        data[i] = 0;
                    }
                }
            }
        }
        cursor.close();
        return data != null ? data : new int[0];
    }

    public long addObjective(int userID, String text, String date) {

        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("user_id", userID);
        values.put("objective_text", text);
        values.put("objective_date", date);
        values.put("event_name", text);
        values.put("completion_time", "");
        values.put("task_type", "Other");

        return db.insert("objectives", null, values);
    }

    public long addTask(
            int userID,
            String eventName,
            String dueDate,
            String completionTime,
            String taskType,
            String objectives) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("user_id", userID);
        values.put("event_name", eventName);
        values.put("objective_date", dueDate);
        values.put("completion_time", completionTime);
        values.put("task_type", taskType);
        values.put("objective_text", objectives);
        return db.insert("objectives", null, values);
    }

    public ArrayList<Objective> getObjectives(int userID) {

        ArrayList<Objective> objectives = new ArrayList<>();

        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(
                "objectives",
                null,
                "user_id=?",
                new String[]{String.valueOf(userID)},
                null,
                null,
                "objective_date ASC"
        );

        while (cursor.moveToNext()) {

            objectives.add(readObjective(cursor));
        }

        cursor.close();

        return objectives;
    }

    public ArrayList<Objective> getObjectivesForDate(int userID, String date) {

        ArrayList<Objective> objectives = new ArrayList<>();

        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(
                "objectives",
                null,
                "user_id=? AND objective_date=?",
                new String[]{String.valueOf(userID), date},
                null,
                null,
                null
        );

        while (cursor.moveToNext()) {

            objectives.add(readObjective(cursor));
        }

        cursor.close();
        return objectives;
    }

    public ArrayList<Objective> getObjectivesForFuture(int userID, String today) {

        ArrayList<Objective> objectives = new ArrayList<>();

        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(
                "objectives",
                null,
                "user_id=? AND objective_date>?",
                new String[]{
                        String.valueOf(userID),
                        today
                },
                null,
                null,
                "objective_date ASC"
        );

        while (cursor.moveToNext()) {

            objectives.add(readObjective(cursor));
        }

        cursor.close();
        return objectives;
    }

    public boolean deleteObjective(int objectiveID){
        SQLiteDatabase db = getWritableDatabase();
        int deleted = db.delete(
                "objectives",
                "id=?",
                new String[]{String.valueOf(objectiveID)}
        );
        return deleted == 1;
    }

    public boolean deleteSession(int sessionID) {
        SQLiteDatabase db = getWritableDatabase();
        int deleted = db.delete(
                "sessions",
                "id=?",
                new String[]{String.valueOf(sessionID)}
        );
        return deleted == 1;
    }

    public boolean updateObjective(int objectiveID, String text, String date) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("objective_text", text);
        values.put("objective_date", date);
        int updated = db.update(
                "objectives",
                values,
                "id=?",
                new String[]{String.valueOf(objectiveID)}
        );
        return updated == 1;
    }

    public boolean updateTask(
            int objectiveID,
            String eventName,
            String dueDate,
            String completionTime,
            String taskType,
            String objectives) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("event_name", eventName);
        values.put("objective_date", dueDate);
        values.put("completion_time", completionTime);
        values.put("task_type", taskType);
        values.put("objective_text", objectives);
        int updated = db.update(
                "objectives",
                values,
                "id=?",
                new String[]{String.valueOf(objectiveID)}
        );
        return updated == 1;
    }

    private Objective readObjective(Cursor cursor) {
        String objectiveText = cursor.getString(
                cursor.getColumnIndexOrThrow("objective_text"));
        String eventName = cursor.getString(
                cursor.getColumnIndexOrThrow("event_name"));
        return new Objective(
                cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                eventName,
                cursor.getString(cursor.getColumnIndexOrThrow("objective_date")),
                cursor.getString(cursor.getColumnIndexOrThrow("completion_time")),
                cursor.getString(cursor.getColumnIndexOrThrow("task_type")),
                objectiveText
        );
    }

    public int deletePastObjectives(int userID, String date) {

        SQLiteDatabase db = getWritableDatabase();

        return db.delete(
                "objectives",
                "user_id=? AND objective_date<?",
                new String[]{
                        String.valueOf(userID),
                        date
                }
        );
    }
}
