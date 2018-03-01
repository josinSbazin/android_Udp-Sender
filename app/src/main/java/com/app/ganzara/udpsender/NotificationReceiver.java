package com.app.ganzara.udpsender;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

public class NotificationReceiver extends BroadcastReceiver {
    private NotificationManager nm;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (nm == null) {
            nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        }

        String response = intent.getStringExtra(Constants.MESSAGE_KEY);

        NotificationCompat.Builder builder;

        if (Build.VERSION.SDK_INT < 26) {
            builder = new NotificationCompat.Builder(context);
        } else {
            NotificationChannel notificationChannel = new NotificationChannel(
                    "defaultId", "default", NotificationManager.IMPORTANCE_DEFAULT);
            nm.createNotificationChannel(notificationChannel);
            builder = new NotificationCompat.Builder(context, "defaultId");
        }

        Intent intentNotification = new Intent(context, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(
                context, 0, intentNotification, PendingIntent.FLAG_CANCEL_CURRENT);

        Notification notification = builder.setContentIntent(contentIntent)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Получен Response")
                .setContentText(response)
                .setDefaults(Notification.DEFAULT_SOUND |
                        Notification.DEFAULT_VIBRATE)
                .setPriority(Notification.PRIORITY_MAX)
                .build();

        notification.notify();
    }
}
