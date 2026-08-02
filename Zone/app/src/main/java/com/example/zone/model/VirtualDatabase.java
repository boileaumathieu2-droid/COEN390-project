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
    public void saveStudySession() {

        String userId = getCurrentUserId();
        StudySessionModel session = StudySessionModel.getInstance();
        db.collection("users")
                .document(userId)
                .collection("studySessions")
                .add(session.toMap());
    }


    public interface StudySessionCallback {
        void onComplete(ArrayList<StudySessionModel> sessions);
    }
}
