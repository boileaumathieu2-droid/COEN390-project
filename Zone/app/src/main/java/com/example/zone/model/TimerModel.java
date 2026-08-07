package com.example.zone.model;

import static androidx.core.content.ContentProviderCompat.requireContext;

import android.Manifest;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;

import com.example.zone.controller.NotificationController;
import com.example.zone.view.MainView;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Application-wide study timer.
 *
 * Activities only display this model. The clock belongs to this singleton, so
 * opening Analytics, Settings, or another app does not pause the countdown.
 */
public final class TimerModel {

    private static final int HEART_RATE_SAMPLE_SECONDS = 5;
    private boolean breakJustFinished;
    private static volatile TimerModel instance;

    private final ScheduledExecutorService clock =
            Executors.newSingleThreadScheduledExecutor(runnable -> {
                Thread thread = new Thread(runnable, "ZoneStudyTimer");
                thread.setDaemon(true);
                return thread;
            });

    private int studyDuration = 1500;
    private int breakDuration = 300;
    private int remainingTime = 1500;
    private boolean running;
    private boolean breakTime;
    private boolean breakEnabled;
    private int sampleSeconds;
    private long lastClockUpdate;

    private StudySessionModel session;
    private StudySessionModel lastCompletedSession;
    private boolean reflectionPending;
    private ScheduledFuture<?> clockTask;
    private Context applicationContext;

    private TimerModel() {
    }

    public static TimerModel getInstance() {
        if (instance == null) {
            synchronized (TimerModel.class) {
                if (instance == null) {
                    instance = new TimerModel();
                }
            }
        }
        return instance;
    }

    public synchronized void initialize(Context context) {
        if (context != null) {
            applicationContext = context.getApplicationContext();
            syncAppBlockingState();
        }
    }

    public synchronized void setStudyDuration(int duration) {
        studyDuration = Math.max(1, duration);
        if (!running && !breakTime) {
            remainingTime = studyDuration;
        }
    }

    public synchronized void setBreakDuration(int duration) {
        breakDuration = Math.max(1, duration);
        if (!running && breakTime) {
            remainingTime = breakDuration;
        }
    }

    public synchronized void setBreakEnabled(boolean enabled) {
        breakEnabled = enabled;
    }

    public synchronized boolean isBreakEnabled() {
        return breakEnabled;
    }

    /** Kept for deterministic unit tests. The app itself uses the clock task. */
    public synchronized boolean tick() {
        if (!running) {
            return false;
        }
        advanceTimer(1);
        lastClockUpdate = System.currentTimeMillis();
        return running;
    }

    private synchronized void updateFromClock() {
        if (!running) {
            return;
        }
        long now = System.currentTimeMillis();
        int elapsedSeconds = (int) ((now - lastClockUpdate) / 1000L);
        if (elapsedSeconds < 1) {
            return;
        }
        lastClockUpdate += elapsedSeconds * 1000L;
        advanceTimer(elapsedSeconds);
    }

    private void advanceTimer(int elapsedSeconds) {
        if (!running || elapsedSeconds <= 0) {
            return;
        }
        int used = Math.min(elapsedSeconds, remainingTime);
        remainingTime -= used;
        sampleSeconds += used;

        while (sampleSeconds >= HEART_RATE_SAMPLE_SECONDS) {
            if (session != null && !breakTime) {
                session.addHeartRateReading();
            }
            sampleSeconds -= HEART_RATE_SAMPLE_SECONDS;
        }

        if (remainingTime <= 0) {
            finishCurrentPeriod();
        }
    }
    private void finishCurrentPeriod() {
        running = false;
        syncAppBlockingState();
        cancelClock();
        lastClockUpdate = 0L;

        if (!breakTime) {
            // Study session finished
            finishStudySession(studyDuration);
            if (breakEnabled) {
                switchToBreak();
            } else {
                breakTime = false;
                remainingTime = studyDuration;
            }

        } else {
            breakJustFinished = true;

            breakTime = false;
            remainingTime = studyDuration;
        }
    }
    public  boolean getBreakJustFinished() {
        return breakJustFinished;
    }
    public void setBreakJustFinished(boolean enable) {
        breakJustFinished = enable;
    }




    private void finishStudySession(int elapsedSeconds) {
        if (session == null) {
            return;
        }

        session.addHeartRateReading();
        session.completeSession();
        session.setDuration(Math.max(0, elapsedSeconds));
        lastCompletedSession = session;
        reflectionPending = true;
        session = null;
    }

    private void switchToBreak() {
        breakTime = true;
        syncAppBlockingState();
        remainingTime = breakDuration;
        sampleSeconds = 0;
    }

    public synchronized void completeSession() {
        updateFromClock();
        running = false;
        syncAppBlockingState();
        cancelClock();
        lastClockUpdate = 0L;

        if (!breakTime) {
            finishStudySession(Math.max(0, studyDuration - remainingTime));
        }

        if (!breakTime && breakEnabled) {
            switchToBreak();
        } else {
            breakTime = false;
            remainingTime = studyDuration;
        }
    }

    public synchronized boolean isRunning() {
        updateFromClock();
        return running;
    }

    public synchronized boolean isStudySessionActive() {
        updateFromClock();
        return running && !breakTime && session != null;
    }

    public synchronized int getRemainingTime() {
        updateFromClock();
        return remainingTime;
    }

    public synchronized void startTimer() {
        if (running) {
            return;
        }

        int duration = breakTime ? breakDuration : studyDuration;
        if (remainingTime <= 0 || remainingTime > duration) {
            remainingTime = duration;
        }

        if (!breakTime) {
            session = new StudySessionModel();
            session.startSession();
        }

        running = true;
        syncAppBlockingState();
        sampleSeconds = 0;
        lastClockUpdate = System.currentTimeMillis();
        startClock();
    }

    /** Starts a fresh study period when the user chooses Extend in reflection. */
    public synchronized void startNewStudySession() {
        running = false;
        cancelClock();
        breakTime = false;
        remainingTime = studyDuration;
        session = null;
        startTimer();
    }

    public synchronized void pauseTimer() {
        updateFromClock();
        running = false;
        syncAppBlockingState();
        cancelClock();
        lastClockUpdate = 0L;
        if (session != null && !breakTime) {
            session.setStatus(StudySessionModel.Status.INACTIVE);
        }
    }

    public synchronized void resumeTimer() {
        if (running || remainingTime <= 0) {
            return;
        }
        running = true;
        syncAppBlockingState();
        lastClockUpdate = System.currentTimeMillis();
        if (session != null && !breakTime) {
            session.setStatus(StudySessionModel.Status.ACTIVE);
        }
        startClock();
    }

    /** Reset discards an unfinished session and does not add it to History. */
    public synchronized void stopAndReset() {
        updateFromClock();
        running = false;
        syncAppBlockingState();
        cancelClock();
        lastClockUpdate = 0L;
        sampleSeconds = 0;
        if (session != null) {
            session.setStatus(StudySessionModel.Status.INACTIVE);
        }
        session = null;
        breakTime = false;
        remainingTime = studyDuration;
    }

    public synchronized void resetTimer() {
        remainingTime = breakTime ? breakDuration : studyDuration;
    }

    private void startClock() {
        cancelClock();
        clockTask = clock.scheduleAtFixedRate(
                this::updateFromClock,
                1,
                1,
                TimeUnit.SECONDS
        );
    }

    private void cancelClock() {
        if (clockTask != null) {
            clockTask.cancel(false);
            clockTask = null;
        }
    }

    private void syncAppBlockingState() {
        if (applicationContext != null) {
            BlockedAppsStore.setStudySessionActive(
                    applicationContext,
                    running && !breakTime && session != null
            );
        }
    }

    public synchronized boolean claimPendingReflection() {
        if (!reflectionPending) {
            return false;
        }
        reflectionPending = false;
        return true;
    }

    public synchronized StudySessionModel applyReflection(
            boolean objectiveMet,
            int productivityRating
    ) {
        if (lastCompletedSession == null) {
            return null;
        }
        lastCompletedSession.setObjectiveMet(objectiveMet);
        lastCompletedSession.setProductivityRating(productivityRating);
        return lastCompletedSession;
    }

    public synchronized void discardLastCompletedSession() {
        lastCompletedSession = null;
        reflectionPending = false;
    }

    public synchronized int getMinutes() {
        return getRemainingTime() / 60;
    }

    public synchronized int getSeconds() {
        return getRemainingTime() % 60;
    }

    public synchronized boolean isBreakTime() {
        return breakTime;
    }

    public synchronized int getStudyDuration() {
        return studyDuration;
    }

    public synchronized int getBreakDuration() {
        return breakDuration;
    }

    public synchronized StudySessionModel getLiveSession() {
        return session;
    }

    public synchronized StudySessionModel getLastCompletedSession() {
        return lastCompletedSession;
    }
}
