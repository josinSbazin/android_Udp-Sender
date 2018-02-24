package com.app.ganzara.udpsender;


import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.app.ganzara.udpsender.model.EndPoint;
import com.app.ganzara.udpsender.model.UDPHelper;

import java.io.IOException;

import static com.app.ganzara.udpsender.Constants.NOTIFY_ID;

public class MainService extends Service {

    private static final String LOG_TAG = "SERVICE";
    private MainApplication application;
    private UDPHelper udp;
    private Thread work;

    public Handler handler;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @SuppressLint("HandlerLeak")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG_TAG, "service started");
        application = (MainApplication) getApplication();
        udp = new UDPHelper();

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                Bundle data = msg.getData();
                String message = data.getString(Constants.MESSAGE_KEY);
                boolean sound = data.getBoolean(Constants.SOUND_KEY);
                onResponse(message, sound);
            }
        };

        String ip = intent.getStringExtra(Constants.IP_KEY);
        final String message = intent.getStringExtra(Constants.MESSAGE_KEY);
        int port = intent.getIntExtra(Constants.PORT_KEY, 0);

        final EndPoint endPoint = new EndPoint(ip, port);

        final int interval = intent.getIntExtra(Constants.INTERVAL_KEY, 1000);
        final boolean sound = intent.getBooleanExtra(Constants.SOUND_KEY, false);

        work = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Log.d(LOG_TAG, "response running");
                        String response = udp.sendAndWait(message, endPoint, interval);
                        Message msg = handler.obtainMessage();
                        Bundle bundle = new Bundle();
                        bundle.putString(Constants.MESSAGE_KEY, response);
                        bundle.putBoolean(Constants.SOUND_KEY, sound);
                        msg.setData(bundle);
                        handler.sendMessage(msg);
                        Thread.sleep(interval);
                    } catch (InterruptedException e) {
                        Log.d(LOG_TAG, "response interrupt");
                        e.printStackTrace();
                        break;
                    } catch (IOException e) {
                        Log.d(LOG_TAG, "response IO exception");
                        e.printStackTrace();
                    } catch (Exception e) {
                        Log.d(LOG_TAG, "response exception");
                        e.printStackTrace();
                    }
                }
            }
        });

        work.start();

        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        Log.d(LOG_TAG, "service destroy");
        super.onDestroy();
        if (work != null) work.interrupt();
    }

    private void onResponse(String response, boolean sound) {
        if (application != null) {
            application.addResponse(response, sound);
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

        Notification notification = builder.setContentIntent(contentIntent)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Получен Response")
                .setContentText(response)
                .setDefaults(Notification.DEFAULT_SOUND |
                        Notification.DEFAULT_VIBRATE)
                .setPriority(Notification.PRIORITY_MAX)
                .build();

        startForeground(Constants.NOTIFY_ID, notification);
    }
}
