package com.app.ganzara.udpsender;

import android.annotation.SuppressLint;
import android.app.Application;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainApplication extends Application {

    public MainActivity currentActivity;
    public Handler handler;

    public List<String> responses = Collections.synchronizedList(new ArrayList<String>());

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
    }
}
