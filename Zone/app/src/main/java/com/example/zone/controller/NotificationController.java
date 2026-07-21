package com.example.zone.controller;
import android.Manifest;
import android.app.NotificationChannel;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.RequiresPermission;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.example.zone.R;

public class NotificationController {

    private static final String CHANNEL_ID = "study_channel";
    private static final int NOTIFICATION_ID = 100;

    private final Context context;


    public NotificationController(Context context) {
        this.context = context;
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    public void sendNotifications(String title, String message) {
        if (!hasNotificationPermission()) {
            return;
        }
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_launcher_foreground)
                        .setContentTitle(title)
                        .setContentText(message)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setAutoCancel(true);
        NotificationManagerCompat manager =
                NotificationManagerCompat.from(context);
        manager.notify(
                NOTIFICATION_ID,
                builder.build()
        );
    }
}