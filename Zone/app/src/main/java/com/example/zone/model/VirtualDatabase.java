package com.example.zone.model;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

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

    public interface DetailedAuthCallback {
        void onResult(boolean success, String message);
    }

    public void createAccount(
            String email,
            String password,
            DetailedAuthCallback callback
    ) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {
                    FirebaseUser currentUser = auth.getCurrentUser();
                    if (currentUser == null) {
                        callback.onResult(false, "Account could not be created. Please try again.");
                        return;
                    }
                    String uid = currentUser.getUid();
                    Map<String, Object> user = new HashMap<>();
                    user.put("email", email);

                    // Authentication is the account authority. The profile document is
                    // useful metadata, but a temporary Firestore failure must not report
                    // that a successfully created account failed.
                    db.collection("users")
                            .document(uid)
                            .set(user)
                            .addOnFailureListener(error -> Log.w(
                                    "VirtualDatabase",
                                    "Account created, but the profile document was not saved",
                                    error
                            ));
                    callback.onResult(true, "Account created successfully.");
                })
                .addOnFailureListener(e -> {
                    if (e instanceof FirebaseAuthUserCollisionException) {
                        callback.onResult(false, "An account already exists for this email.");
                    } else if (e instanceof FirebaseAuthWeakPasswordException) {
                        callback.onResult(false, "Use a password with at least 6 characters.");
                    } else if (e instanceof FirebaseAuthInvalidCredentialsException) {
                        callback.onResult(false, "Enter a valid email address.");
                    } else {
                        String message = e.getLocalizedMessage();
                        callback.onResult(
                                false,
                                message == null || message.trim().isEmpty()
                                        ? "Account creation failed. Please try again."
                                        : message
                        );
                    }
                });
    }

    /** Compatibility overload retained for older callers. */
    public void createAccount(
            String email,
            String password,
            Context context,
            AuthCallback callback
    ) {
        createAccount(email, password, (success, message) -> callback.onResult(success));
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

    public void sendPasswordResetEmail(String email, DetailedAuthCallback callback) {
        auth.sendPasswordResetEmail(email)
                .addOnSuccessListener(unused -> callback.onResult(
                        true,
                        "Password reset email sent. Check your inbox and spam folder."
                ))
                .addOnFailureListener(error -> {
                    String message = error.getLocalizedMessage();
                    callback.onResult(
                            false,
                            message == null || message.trim().isEmpty()
                                    ? "Password reset email could not be sent."
                                    : message
                    );
                });
    }

    public void deleteAccount(DetailedAuthCallback callback) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            callback.onResult(false, "No signed-in account was found.");
            return;
        }

        String userId = currentUser.getUid();
        DocumentReference userDocument = db.collection("users").document(userId);

        deleteCollection(userDocument.collection("objectives"))
                .continueWithTask(task -> requireSuccessful(task,
                        () -> deleteCollection(userDocument.collection("studySessions"))))
                .continueWithTask(task -> requireSuccessful(task,
                        () -> deleteCollection(userDocument.collection("timerSettings"))))
                .continueWithTask(task -> requireSuccessful(task,
                        () -> deleteSubjects(userDocument.collection("Subjects"))))
                .continueWithTask(task -> requireSuccessful(task,
                        () -> deleteSubjects(userDocument.collection("subjects"))))
                .continueWithTask(task -> requireSuccessful(task, userDocument::delete))
                .continueWithTask(task -> requireSuccessful(task, currentUser::delete))
                .addOnSuccessListener(unused -> callback.onResult(
                        true,
                        "Your account and Zone data were deleted."
                ))
                .addOnFailureListener(error -> {
                    if (error instanceof FirebaseAuthRecentLoginRequiredException) {
                        callback.onResult(
                                false,
                                "For security, sign out and sign in again before deleting your account."
                        );
                    } else {
                        String message = error.getLocalizedMessage();
                        callback.onResult(
                                false,
                                message == null || message.trim().isEmpty()
                                        ? "The account could not be deleted. Please try again."
                                        : message
                        );
                    }
                });
    }

    private Task<Void> requireSuccessful(
            Task<?> previous,
            Supplier<Task<Void>> next
    ) {
        if (!previous.isSuccessful()) {
            Exception error = previous.getException();
            return Tasks.forException(error == null
                    ? new IllegalStateException("Cloud data operation failed")
                    : error);
        }
        return next.get();
    }

    private Task<Void> deleteCollection(CollectionReference collection) {
        return collection.get().continueWithTask(queryTask -> {
            if (!queryTask.isSuccessful()) {
                Exception error = queryTask.getException();
                return Tasks.forException(error == null
                        ? new IllegalStateException("Could not load account data")
                        : error);
            }
            if (queryTask.getResult().isEmpty()) {
                return Tasks.forResult(null);
            }
            WriteBatch batch = db.batch();
            for (DocumentSnapshot document : queryTask.getResult().getDocuments()) {
                batch.delete(document.getReference());
            }
            return batch.commit();
        });
    }

    private Task<Void> deleteSubjects(CollectionReference subjects) {
        return subjects.get().continueWithTask(queryTask -> {
            if (!queryTask.isSuccessful()) {
                Exception error = queryTask.getException();
                return Tasks.forException(error == null
                        ? new IllegalStateException("Could not load subjects")
                        : error);
            }
            List<Task<?>> deletions = new ArrayList<>();
            for (DocumentSnapshot subject : queryTask.getResult().getDocuments()) {
                deletions.add(deleteCollection(subject.getReference().collection("Grades"))
                        .continueWithTask(task -> requireSuccessful(
                                task,
                                () -> deleteCollection(subject.getReference().collection("grades"))
                        ))
                        .continueWithTask(task -> requireSuccessful(
                                task,
                                subject.getReference()::delete
                        )));
            }
            return Tasks.whenAll(deletions);
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
        if (userId == null) {
            callback.onComplete(new ArrayList<>());
            return;
        }
        String requestedDate = normalizeObjectiveDate(date);
        db.collection("users")
                .document(userId)
                .collection("objectives")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    ArrayList<Objective> objectives = new ArrayList<>();
                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                        Objective objective = parseObjective(document);
                        if (normalizeObjectiveDate(objective.getObjectiveDate())
                                .compareTo(requestedDate) > 0) {
                            objectives.add(objective);
                        }
                    }
                    callback.onComplete(objectives);
                })
                .addOnFailureListener(error -> callback.onComplete(new ArrayList<>()));
    }
    public interface ObjectiveCallback {
        void onComplete(ArrayList<Objective> objectives);
    }

    public void getObjectives(ObjectiveCallback callback) {
        String userId = getCurrentUserId();
        if (userId == null) {
            callback.onComplete(new ArrayList<>());
            return;
        }
        db.collection("users")
                .document(userId)
                .collection("objectives")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    ArrayList<Objective> objectives = new ArrayList<>();
                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                        objectives.add(parseObjective(document));
                    }
                    callback.onComplete(objectives);
                })
                .addOnFailureListener(error -> callback.onComplete(new ArrayList<>()));
    }
    public void GetDailyObjectives(ObjectiveCallback callback, String date) {
        String userId = getCurrentUserId();
        if (userId == null) {
            callback.onComplete(new ArrayList<>());
            return;
        }
        String requestedDate = normalizeObjectiveDate(date);
        db.collection("users")
                .document(userId)
                .collection("objectives")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    ArrayList<Objective> objectives = new ArrayList<>();
                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                        Objective objective = parseObjective(document);
                        if (requestedDate.equals(normalizeObjectiveDate(
                                objective.getObjectiveDate()))) {
                            objectives.add(objective);
                        }
                    }
                    callback.onComplete(objectives);
                })
                .addOnFailureListener(error -> callback.onComplete(new ArrayList<>()));
    }
    public void saveObjective(String objective, String date, String name, String time, String type) {
        saveObjective(objective, date, name, time, type, success -> { });
    }

    public void saveObjective(
            String objective,
            String date,
            String name,
            String time,
            String type,
            AuthCallback callback
    ) {
        String userId = getCurrentUserId();
        if (userId == null) {
            callback.onResult(false);
            return;
        }
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
        data.put("completed", false);
        docRef.set(data)
                .addOnSuccessListener(unused -> callback.onResult(true))
                .addOnFailureListener(error -> callback.onResult(false));
    }

    public void deleteTask(String objective) {
        deleteTask(objective, success -> { });
    }

    public void deleteTask(String objective, AuthCallback callback) {
        String userId = getCurrentUserId();
        if (userId == null || objective == null) {
            callback.onResult(false);
            return;
        }
        db.collection("users")
                .document(userId)
                .collection("objectives")
                .document(objective)
                .delete()
                .addOnSuccessListener(unused -> callback.onResult(true))
                .addOnFailureListener(error -> callback.onResult(false));
    }
    public void editTask(String id, String objective, String date,
                         String name, String time, String type) {
        editTask(id, objective, date, name, time, type, success -> { });
    }

    public void editTask(
            String id,
            String objective,
            String date,
            String name,
            String time,
            String type,
            AuthCallback callback
    ) {

        String userId = getCurrentUserId();
        if (userId == null) {
            callback.onResult(false);
            return;
        }

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
                .addOnSuccessListener(unused -> {
                    Log.d("Firestore", "Task updated");
                    callback.onResult(true);
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error updating task", e);
                    callback.onResult(false);
                });
    }

    public void updateObjectiveCompletion(String id, boolean completed, AuthCallback callback) {
        String userId = getCurrentUserId();
        if (userId == null) {
            callback.onResult(false);
            return;
        }
        db.collection("users")
                .document(userId)
                .collection("objectives")
                .document(id)
                .update("completed", completed)
                .addOnSuccessListener(unused -> callback.onResult(true))
                .addOnFailureListener(e -> callback.onResult(false));
    }
    public static boolean isInternetAvailable(Context context) {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
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
    public interface GradeEditCallback {
        void onComplete(boolean success);
    }
    public void editGrade(GradeEditCallback callback, String subjectId, String gradeId, int grade, String type) {
        String userId = getCurrentUserId();
        Map<String, Object> updates = new HashMap<>();
        updates.put("grade", grade);
        updates.put("type", type);
        db.collection("users")
                .document(userId)
                .collection("Subjects")
                .document(subjectId)
                .collection("Grades")
                .document(gradeId)
                .update(updates)
                .addOnSuccessListener(unused -> {
                    callback.onComplete(true);
                })
                .addOnFailureListener(e -> {
                    System.out.println("Edit grade failed: " + e.getMessage());
                    callback.onComplete(false);
                });
    }

    public void saveSubject(String subjectName) {
        saveSubject(subjectName, success -> { });
    }

    public void saveSubject(String subjectName, AuthCallback callback) {
        String userId = getCurrentUserId();
        if (userId == null) {
            callback.onResult(false);
            return;
        }
     DocumentReference docRef = db.collection("users")
                .document(userId)
                .collection("Subjects")
                .document();
     String id = docRef.getId();
     Map <String, Object> data = new HashMap<>();
     data.put("SubjectID", id);
     data.put("SubjectName", subjectName);
     docRef.set(data)
             .addOnSuccessListener(unused -> callback.onResult(true))
             .addOnFailureListener(error -> callback.onResult(false));
    }
    public void saveGrade(String type, String grade, String SubjectId) {
        saveGrade(type, grade, SubjectId, success -> { });
    }

    public void saveGrade(
            String type,
            String grade,
            String SubjectId,
            AuthCallback callback
    ) {
        String userId = getCurrentUserId();
        if (userId == null) {
            callback.onResult(false);
            return;
        }
        DocumentReference docRef = db.collection("users")
                .document(userId)
                .collection("Subjects")
                .document(SubjectId)
                .collection("Grades")
                .document();
        String id  =docRef.getId();
        Map <String, Object> data = new HashMap<>();
        data.put("GradeId", id);
        data.put("type", type);
        data.put("grade", grade);
        docRef.set(data)
                .addOnSuccessListener(unused -> callback.onResult(true))
                .addOnFailureListener(error -> callback.onResult(false));
    }

    public void deleteSubject(String subject) {
        deleteSubject(subject, success -> { });
    }

    public void deleteSubject(String subject, AuthCallback callback) {
        String userId = getCurrentUserId();
        if (userId == null || subject == null) {
            callback.onResult(false);
            return;
        }
        DocumentReference subjectDocument = db.collection("users")
                .document(userId)
                .collection("Subjects")
                .document(subject);
        deleteCollection(subjectDocument.collection("Grades"))
                .continueWithTask(task -> requireSuccessful(
                        task,
                        () -> deleteCollection(subjectDocument.collection("grades"))
                ))
                .continueWithTask(task -> requireSuccessful(
                        task,
                        subjectDocument::delete
                ))
                .addOnSuccessListener(unused -> callback.onResult(true))
                .addOnFailureListener(error -> callback.onResult(false));
    }
    public interface SubjectsCallback {
        void onSubjectsLoaded(ArrayList<Subject> subjects);
    }
    public interface SubjectExistsCallback {
        void onResult(boolean exists);
    }
    public void checkIfSubjectExists(String subjectName, SubjectExistsCallback callback) {

        String userId = getCurrentUserId();

        db.collection("users")
                .document(userId)
                .collection("Subjects")
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    for (DocumentSnapshot doc : querySnapshot) {

                        String name = doc.getString("SubjectName");

                        if (subjectName.equals(name)) {
                            callback.onResult(true);
                            return;
                        }
                    }

                    callback.onResult(false);
                })
                .addOnFailureListener(e -> {
                    callback.onResult(false);
                });
    }

    public void setTimerSettings(double studyTime, double breakTime, boolean isBreakEnabled) {
        String userId = getCurrentUserId();
        Map<String, Object> data = new HashMap<>();
        data.put("studyTime", studyTime);
        data.put("breakTime", breakTime);
        data.put("isBreakEnabled", isBreakEnabled);
        db.collection("users")
                .document(userId)
                .collection("timerSettings")
                .document("settings")
                .set(data);
        }
    public interface TimerSettingsCallback {
        void onComplete();
    }




    public void getSubjects(SubjectsCallback callback) {
        String userId = getCurrentUserId();
        if (userId == null) {
            callback.onSubjectsLoaded(new ArrayList<>());
            return;
        }
        db.collection("users")
                .document(userId)
                .collection("Subjects")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    ArrayList<Subject> subjects = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot) {
                        String subjectId = doc.getId();
                        String subjectName = doc.getString("SubjectName");
                        Subject subject = new Subject(subjectName, subjectId);
                        subjects.add(subject);
                    }

                    callback.onSubjectsLoaded(subjects);
                })
                .addOnFailureListener(e -> {
                    callback.onSubjectsLoaded(new ArrayList<>());
                });
    }

    private Objective parseObjective(DocumentSnapshot document) {
        return new Objective(
                valueOrFallback(document.getString("objectiveID"), document.getId()),
                valueOrFallback(document.getString("eventName"), "Untitled task"),
                normalizeObjectiveDate(document.getString("objectiveDate")),
                valueOrFallback(document.getString("completionTime"), ""),
                valueOrFallback(document.getString("taskType"), "Other"),
                valueOrFallback(document.getString("objectiveText"), ""),
                document.getBoolean("completed") != null && document.getBoolean("completed")
        );
    }

    private String valueOrFallback(String value, String fallback) {
        return value == null ? fallback : value;
    }

    private String normalizeObjectiveDate(String value) {
        if (value == null) {
            return "";
        }
        String normalized = value.trim();
        return normalized.length() >= 10
                ? normalized.substring(0, 10)
                : normalized;
    }
//    public void getObjectives(ObjectiveCallback callback) {
//        String userId = getCurrentUserId();
//        db.collection("users")
//                .document(userId)
//                .collection("objectives")
//                .get()
//                .addOnSuccessListener(querySnapshot -> {
//                    ArrayList<Objective> objectives = new ArrayList<>();
//                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
//                        String objectiveID = document.getString("objectiveID");
//                        String objectiveText = document.getString("objectiveText");
//                        String objectiveDate = document.getString("objectiveDate");
//                        String eventName = document.getString("eventName");
//                        String completionTime = document.getString("completionTime");
//                        String taskType = document.getString("taskType");
//                        Objective objective = new Objective(
//                                objectiveID,
//                                eventName,
//                                objectiveDate,
//                                completionTime,
//                                taskType,
//                                objectiveText
//                        );
//                        objectives.add(objective);
//                    }
//                    callback.onComplete(objectives);
//                });
//    }
}
