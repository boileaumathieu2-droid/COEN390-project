package com.example.zone.model;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
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
    public String getCurrentUserID() {
        if (auth.getCurrentUser() == null) {
            return null;
        }
        return auth.getCurrentUser().getUid();
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

    public String getCurrentUserEmail() {
        return auth.getCurrentUser() == null
                ? null : auth.getCurrentUser().getEmail();
    }

    public void signOut() {
        auth.signOut();
    }

    public void verifyConnection() {
        Map<String, Object> user = new HashMap<>();
        user.put("key1", "it works");
        db.collection("users")
                .document("testUser")
                .set(user)
                .addOnSuccessListener(unused -> System.out.println("Data saved!!"))
                .addOnFailureListener(e -> System.out.println("FAILED!!!!"));
    }

    public void insertStudySession(StudySessionModel session) {
        String uid = getCurrentUserId();
        if (uid == null) {
            return;
        }
        db.collection("users")
                .document(uid)
                .collection("studySessions")
                .add(session.toMap())
                .addOnSuccessListener(unused ->
                        System.out.println("Session saved"))
                .addOnFailureListener(e ->
                        System.out.println("Failed: " + e.getMessage()));
    }

    public void getStudySessions(StudySessionCallback callback) {
        String userId = getCurrentUserId();
        if (userId == null) {
            callback.onComplete(new ArrayList<>());
            return;
        }

        db.collection("users")
                .document(userId)
                .collection("studySessions")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    ArrayList<StudySessionModel> sessions = new ArrayList<>();
                    for (DocumentSnapshot document : querySnapshot) {
                        StudySessionModel session = parseSession(document);
                        if (session != null) {
                            sessions.add(session);
                        }
                    }
                    callback.onComplete(sessions);
                })
                .addOnFailureListener(e -> {
                    System.err.println("Error fetching sessions: " + e.getMessage());
                    callback.onComplete(new ArrayList<>());
                });
    }

    private StudySessionModel parseSession(DocumentSnapshot doc) {
        try {
            StudySessionModel session = new StudySessionModel();
            session.setDocumentId(doc.getId());
            
            String startTime = doc.getString("startTime");
            if (startTime != null) session.setStartTime(java.time.LocalDateTime.parse(startTime));
            
            String endTime = doc.getString("endTime");
            if (endTime != null) session.setEndTime(java.time.LocalDateTime.parse(endTime));
            
            Long duration = doc.getLong("duration");
            if (duration != null) session.setDuration(duration.intValue());
            
            String status = doc.getString("status");
            if (status != null) session.setStatus(StudySessionModel.Status.valueOf(status));
            
            Boolean objectiveMet = doc.getBoolean("objectiveMet");
            if (objectiveMet != null) session.setObjectiveMet(objectiveMet);
            
            Long rating = doc.getLong("productivityRating");
            if (rating != null) session.setProductivityRating(rating.intValue());
            
            Long avgHR = doc.getLong("averageHeartRate");
            if (avgHR != null) session.setHeartRate(avgHR.intValue());
            
            Long restingHR = doc.getLong("restingHeartRate");
            if (restingHR != null) session.setRestingHeartRate(restingHR.intValue());

            Long maxHR = doc.getLong("maxHeartRate");
            if (maxHR != null) session.setMaxHeartRate(maxHR.intValue());

            Long minHR = doc.getLong("minHeartRate");
            if (minHR != null) session.setMinHeartRate(minHR.intValue());

            // Handle heart rate data list
            Object hrDataObj = doc.get("heartRateData");
            if (hrDataObj instanceof java.util.List) {
                java.util.List<?> list = (java.util.List<?>) hrDataObj;
                for (Object item : list) {
                    if (item instanceof Long) {
                        session.addHistoricalHeartRate(((Long) item).intValue());
                    }
                }
            }

            return session;
        } catch (Exception e) {
            System.err.println("Error parsing session: " + e.getMessage());
            return null;
        }
    }
    public void saveStudySession() {
        saveStudySession(StudySessionModel.getInstance(), success -> { });
    }

    public void saveStudySession(
            StudySessionModel session,
            AuthCallback callback
    ) {
        String userId = getCurrentUserId();
        if (userId == null || session == null) {
            callback.onResult(false);
            return;
        }
        db.collection("users")
                .document(userId)
                .collection("studySessions")
                .add(session.toMap())
                .addOnSuccessListener(document -> {
                    session.setDocumentId(document.getId());
                    callback.onResult(true);
                })
                .addOnFailureListener(error -> callback.onResult(false));
    }


    public void deleteStudySession(String documentId, AuthCallback callback) {
        String userId = getCurrentUserId();
        if (userId == null || documentId == null) {
            callback.onResult(false);
            return;
        }

        db.collection("users")
                .document(userId)
                .collection("studySessions")
                .document(documentId)
                .delete()
                .addOnSuccessListener(unused -> callback.onResult(true))
                .addOnFailureListener(e -> callback.onResult(false));
    }



    public interface StudySessionCallback {
        void onComplete(ArrayList<StudySessionModel> sessions);
    }
    public void GetFutureObjectives(ObjectiveCallback callback, String date) {
        String userId = getCurrentUserId();
        db.collection("users")
                .document(userId)
                .collection("objectives")
                .whereNotEqualTo("objectiveDate", date)
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
                        String objectiveID = document.getString("objectiveID");
                        String objectiveText = document.getString("objectiveText");
                        String objectiveDate = document.getString("objectiveDate");
                        String eventName = document.getString("eventName");
                        String completionTime = document.getString("completionTime");
                        String taskType = document.getString("taskType");
                        Objective objective = new Objective(
                                objectiveID,
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
    public void GetDailyObjectives(ObjectiveCallback callback, String date) {
        String userId = getCurrentUserId();
        db.collection("users")
                .document(userId)
                .collection("objectives")
                .whereEqualTo("objectiveDate", date)
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
    public void saveObjective(String objective, String date, String name, String time, String type) {
        String userId = getCurrentUserId();
        DocumentReference docRef = db.collection("users")
                .document(userId)
                .collection("objectives")
                .document();
        String id = docRef.getId();
        Map<String, Object> data = new HashMap<>();
        data.put("objectiveID", id);
        data.put("objectiveText", objective);
        data.put("objectiveDate", date);
        data.put("eventName", name);
        data.put("completionTime", time);
        data.put("taskType", type);
        docRef.set(data);
    }

    public void deleteTask(String objective) {
        String userId = getCurrentUserId();
        db.collection("users")
                .document(userId)
                .collection("objectives")
                .document(objective)
                .delete();
    }
    public void editTask(String id, String objective, String date,
                         String name, String time, String type) {

        String userId = getCurrentUserId();

        Map<String, Object> data = new HashMap<>();
        data.put("objectiveText", objective);
        data.put("objectiveDate", date);
        data.put("eventName", name);
        data.put("completionTime", time);
        data.put("taskType", type);
        db.collection("users")
                .document(userId)
                .collection("objectives")
                .document(id)
                .update(data)
                .addOnSuccessListener(unused ->
                        Log.d("Firestore", "Task updated"))
                .addOnFailureListener(e ->
                        Log.e("Firestore", "Error updating task", e));
    }
    public static boolean isInternetAvailable(Context context) {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }
}



