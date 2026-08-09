package com.example.zone.controller;
import android.Manifest;
import android.app.NotificationChannel;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.zone.R;
import com.example.zone.view.LoginView;

import java.util.concurrent.atomic.AtomicInteger;

public class NotificationController {

    private static final String CHANNEL_ID = "study_channel";
    private static final AtomicInteger NEXT_NOTIFICATION_ID =
            new AtomicInteger(100);

    private final Context context;


    public NotificationController(Context context) {
        this.context = context;
        Log.d("Notification", "NotificationController constructor");
        createNotificationChannel();
    }

    public void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel =
                    new NotificationChannel(
                            CHANNEL_ID,
                            "Study Notifications",
                            android.app.NotificationManager.IMPORTANCE_DEFAULT
                    );
            channel.setDescription(
                    "Notifications for study sessions and breaks"
            );
            android.app.NotificationManager manager =
                    context.getSystemService(android.app.NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    public boolean hasNotificationPermission() {
//        prefs = context.getSharedPreferences("Settings", Context.MODE_PRIVATE);
//        boolean Notifications = prefs.getBoolean("Notifications", false);
//        if (!Notifications) {
//            return false;
//        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }


    public void sendNotifications(String title, String message) {
        if (!hasNotificationPermission()) {
            Log.d("Notification", "No notification permission");
            return;
        }

        Intent openApp = new Intent(context, LoginView.class)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(
                context,
                NEXT_NOTIFICATION_ID.get(),
                openApp,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_launcher_foreground)
                        .setContentTitle(title)
                        .setContentText(message)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setContentIntent(contentIntent)
                        .setAutoCancel(true);

        NotificationManagerCompat manager =
                NotificationManagerCompat.from(context);

        Log.d("Notification", "Posting notification");
        try {
            manager.notify(NEXT_NOTIFICATION_ID.incrementAndGet(), builder.build());
        } catch (SecurityException error) {
            Log.w("Notification", "Notification permission was revoked", error);
        }
    }
}
