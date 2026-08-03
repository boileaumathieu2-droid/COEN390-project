package com.example.zone.model;

import android.content.Context;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class VirtualDatabase {

    private final FirebaseFirestore db;
    private final FirebaseAuth auth;

    public VirtualDatabase() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    public interface AuthCallback {
        void onResult(boolean success);
    }

    public void createAccount(String email, String password, Context context, AuthCallback callback) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {
                    assert auth.getCurrentUser() != null;
                    String uid = auth.getCurrentUser().getUid();
                    Map<String, Object> user = new HashMap<>();
                    user.put("email", email);

                    db.collection("users")
                            .document(uid)
                            .set(user)
                            .addOnSuccessListener(unused -> {
                                callback.onResult(true);
                            })
                            .addOnFailureListener(e -> callback.onResult(false));
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context,
                            "Account creation failed",
                            Toast.LENGTH_SHORT).show();

                    callback.onResult(false);
                });
    }

    public void signIn(String email,
                       String password,
                       AuthCallback callback) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {
                    callback.onResult(true);
                })
                .addOnFailureListener(e -> {
                    callback.onResult(false);
                });
    }

    public String getCurrentUserId() {
        if (auth.getCurrentUser() == null) {
            return null;
        }
        return auth.getCurrentUser().getUid();
    }

    public void signOut() {
        auth.signOut();
    }

    public interface StudySessionCallback {
        void onComplete(ArrayList<StudySessionModel> sessions);
    }

    public void insertStudySession(StudySessionModel session) {
        String uid = getCurrentUserId();
        if (uid == null) {
            return;
        }
        db.collection("studySessions")
                .document(uid)
                .set(session)
                .addOnSuccessListener(unused ->
                        System.out.println("Session saved"))
                .addOnFailureListener(e ->
                        System.out.println("Failed: " + e.getMessage()));
    }

    public void getStudySessions(StudySessionCallback callback) {

        db.collection("studySessions")
                .whereEqualTo("userId", getCurrentUserId())
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    ArrayList<StudySessionModel> sessions = new ArrayList<>();

                    for (DocumentSnapshot document : querySnapshot) {
                        StudySessionModel session =
                                document.toObject(StudySessionModel.class);
                        sessions.add(session);
                    }
                    callback.onComplete(sessions);
                });
    }
    public void DeleteStudySession(String Session) {
        String UserId = getCurrentUserId();
        db.collection("users")
                .document(UserId)
                .collection("studySessions")
                .document(Session)
                .delete();
    }
    public void saveStudySession() {

        String userId = getCurrentUserId();
        StudySessionModel session = StudySessionModel.getInstance();
        db.collection("users")
                .document(userId)
                .collection("studySessions")
                .add(session.toMap());
    }

    public void saveSubject(String subjectName) {
        String userId = getCurrentUserId();
        Map<String, Object> data = new HashMap<>();
        data.put("name", subjectName);
        db.collection("users")
                .document(userId)
                .collection("Subjects")
                .document(subjectName)
                .set(data);
    }

    public void saveGrade(String subjectId, String assignment, double grade) {
        String userId = getCurrentUserId();
        Map<String, Object> data = new HashMap<>();
        data.put("assignment", assignment);
        data.put("grade", grade);

        db.collection("users")
                .document(userId)
                .collection("Subjects")
                .document(subjectId)
                .collection("Grades")
                .add(data);
    }

    public void saveObjective(String objective, String date, String name, String time, String type) {
        String UserId = getCurrentUserId();
        Map<String, Object> data = new HashMap<>();
        data.put("objective_text", objective);
        data.put("objective date", date);
        data.put("event name", name);
        data.put("completion time", time);
        data.put("task type", type);
        db.collection("users")
                .document(UserId)
                .collection("objectives")
                .add(data);
    }


    public void deleteSubject(String subject) {
        String userId = getCurrentUserId();
        db.collection("users")
                .document(userId)
                .collection("subjects")
                .document(subject)
                .delete();
    }

    public void deleteGrade(String subjectId, String gradeId) {
        String userId = getCurrentUserId();
        db.collection("users")
                .document(userId)
                .collection("subjects")
                .document(subjectId)
                .collection("grades")
                .document(gradeId)
                .delete();
    }

    public void deleteObjective(String objective) {
        String userId = getCurrentUserId();
        db.collection("users")
                .document(userId)
                .collection("objectives")
                .document(objective)
                .delete();
    }

    public void getSubjects() {
        String userId = getCurrentUserId();
        db.collection("users")
                .document(userId)
                .collection("Subjects")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (DocumentSnapshot doc : querySnapshot) {

                        String subjectId = doc.getId();
                        String subjectName = doc.getString("name");

                        System.out.println(subjectName + " -> " + subjectId);
                    }
                });

    }

    public interface ObjectiveCallback {
        void onComplete(ArrayList<Objective> objectives);
    }

    public void getObjectives(ObjectiveCallback callback) {

        String userId = getCurrentUserId();

        db.collection("users")
                .document(userId)
                .collection("objectives")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    ArrayList<Objective> objectives = new ArrayList<>();
                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                        String objectiveText = document.getString("objective_text");
                        String objectiveDate = document.getString("objective date");
                        String eventName = document.getString("event name");
                        String completionTime = document.getString("completion time");
                        String taskType = document.getString("task type");
                        Objective objective = new Objective(
                                0, // Firestore doesn't store your local SQLite ID
                                eventName,
                                objectiveDate,
                                completionTime,
                                taskType,
                                objectiveText
                        );
                        objectives.add(objective);
                    }
                    callback.onComplete(objectives);
                });
    }

    public interface GradesCallback {
        void onComplete(ArrayList<String> Grades);
    }

    public void getGrades(GradesCallback callback, String subjectId) {
        String userId = getCurrentUserId();
        db.collection("users")
                .document(userId)
                .collection("Subjects")
                .document(subjectId)
                .collection("Grades")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    ArrayList<String> Grades = new ArrayList<>();
                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                        String grade = document.getString("grade");
                        Grades.add(grade);
                    }
                    callback.onComplete(Grades);
                });
    }
    public void GetDailyObjectives(ObjectiveCallback callback, String date) {
        String userId = getCurrentUserId();
        db.collection("users")
                .document(userId)
                .collection("objectives")
                .whereEqualTo("objective date", date)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    ArrayList<Objective> objectives = new ArrayList<>();
                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                        Objective objective = document.toObject(Objective.class);
                        objectives.add(objective);
                    }
                    callback.onComplete(objectives);
                });
    }
    public void GetFutureObjectives(ObjectiveCallback callback, String date) {
        String userId = getCurrentUserId();
        db.collection("users")
                .document(userId)
                .collection("objectives")
                .whereNotEqualTo("objective date", date)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    ArrayList<Objective> objectives = new ArrayList<>();
                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                        Objective objective = document.toObject(Objective.class);
                        objectives.add(objective);
                    }
                    callback.onComplete(objectives);
                });
    }
}

