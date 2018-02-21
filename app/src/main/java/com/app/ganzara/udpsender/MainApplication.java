package com.app.ganzara.udpsender;

import android.annotation.SuppressLint;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.app.ganzara.udpsender.Constants.NOTIFY_ID;

public class MainApplication extends Application {

    public MainActivity currentActivity;
    public Handler handler;

    private volatile List<String> responses = Collections.synchronizedList(new ArrayList<String>());

    @SuppressLint("HandlerLeak")
    @Override
    public void onCreate() {
        super.onCreate();

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                Bundle data = msg.getData();
                String message = data.getString(Constants.MESSAGE_KEY);
                boolean sound = data.getBoolean(Constants.SOUND_KEY);
                addResponse(message, sound);
            }
        };
    }

    public List<String> getResponses() {
        return responses;
    }

    public void addResponse(String response, boolean sound) {
        if (response == null) {
            response = "null";
        }
        responses.add(response);
        if (currentActivity != null) {
            currentActivity.addResponseNotify();
        }

        if (sound) {
            sendNotification(response);
        }
    }

    private void sendNotification(String response) {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this,
                0, notificationIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder builder;

        if (Build.VERSION.SDK_INT < 26) {
            builder = new NotificationCompat.Builder(this);
        } else {
            NotificationChannel notificationChannel = new NotificationChannel("defaultId", "default", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(notificationChannel);
            builder = new NotificationCompat.Builder(this, "defaultId");
        }

        builder.setContentIntent(contentIntent)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Получен Response")
                .setContentText(response)
        .setDefaults( Notification.DEFAULT_SOUND |
                Notification.DEFAULT_VIBRATE);

        notificationManager.notify(NOTIFY_ID, builder.build());
    }


}
