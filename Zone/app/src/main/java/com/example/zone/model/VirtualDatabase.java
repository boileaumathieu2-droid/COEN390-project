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
}
