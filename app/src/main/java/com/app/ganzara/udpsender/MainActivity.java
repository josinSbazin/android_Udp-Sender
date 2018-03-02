package com.app.ganzara.udpsender;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.app.ganzara.udpsender.model.Validation;

public class MainActivity extends AppCompatActivity {

    private EditText etIp;
    private EditText etPort;
    private EditText etInterval;
    private EditText etMessge;
    private CheckBox cbSound;
    private ListView listView;

    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etIp = findViewById(R.id.et_ip);
        etPort = findViewById(R.id.et_port);
        etInterval = findViewById(R.id.et_interval);
        etMessge = findViewById(R.id.et_message);
        cbSound = findViewById(R.id.cb_sound);
        listView = findViewById(R.id.lv_responses);

        ((MainApplication) getApplication()).currentActivity = this;

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, ((MainApplication) getApplication()).getResponses());
        listView.setAdapter(adapter);

    }

    @Override
    protected void onStart() {
        super.onStart();

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            @SuppressLint("BatteryLife") Intent intent = new
                    Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                    Uri.parse("package:" + getPackageName()));
            startActivity(intent);
        }

    }

    public void onClickStart(View v) {
        // проверки
        String ip = etIp.getText().toString();
        String validIpMsg = Validation.validateIP(this, ip);
        if (validIpMsg != null) {
            showMessage(validIpMsg);
            return;
        }

        String strPort = etPort.getText().toString();
        String validPortMsg = Validation.validatePort(this, strPort);
        if (validPortMsg != null) {
            showMessage(validPortMsg);
            return;
        }
        int port = Integer.parseInt(strPort);

        String intervalStr = etInterval.getText().toString();
        String validIntevalMsg = Validation.validateInterval(this, intervalStr);
        if (validIntevalMsg != null) {
            showMessage(validIntevalMsg);
            return;
        }
        int interval = Integer.parseInt(intervalStr);

        String message = etMessge.getText().toString();
        if (message.isEmpty()) {
            showMessage("Сообщение не может быть пустым");
            return;
        }

        boolean playSound = cbSound.isChecked();

        Intent intent = new Intent(this, MainService.class);
        intent.putExtra(Constants.IP_KEY, ip);
        intent.putExtra(Constants.PORT_KEY, port);
        intent.putExtra(Constants.INTERVAL_KEY, interval * 1000);
        intent.putExtra(Constants.MESSAGE_KEY, message);
        intent.putExtra(Constants.SOUND_KEY, playSound);

        startService(intent);
    }

    public void onClickStop(View v) {
        stopService(new Intent(this, MainService.class));
    }

    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    public void addResponseNotify() {
        adapter.notifyDataSetChanged();
    }
}
