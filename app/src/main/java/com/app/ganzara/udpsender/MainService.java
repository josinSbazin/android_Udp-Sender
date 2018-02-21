package com.app.ganzara.udpsender;


import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;

import com.app.ganzara.udpsender.model.EndPoint;
import com.app.ganzara.udpsender.model.UDPHelper;

import java.io.IOException;

public class MainService extends Service {

    private MainApplication application;
    private UDPHelper udp;
    private Thread work;

    @Override
    public void onCreate() {
        super.onCreate();
        application = (MainApplication) getApplication();
        udp = new UDPHelper();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
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
                        String response = udp.sendAndWait(message, endPoint, interval);
                        Handler handler = application.handler;
                        Message msg = handler.obtainMessage();
                        Bundle bundle = new Bundle();
                        bundle.putString(Constants.MESSAGE_KEY, response);
                        bundle.putBoolean(Constants.SOUND_KEY, sound);
                        msg.setData(bundle);
                        handler.sendMessage(msg);
                        Thread.sleep(interval);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        break;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        work.start();

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (work != null) work.interrupt();
    }
}
