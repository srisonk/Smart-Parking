package com.example.srisonkadariya.smartparking;

import android.content.Intent;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ProgressBar;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.JSONException;
import org.json.JSONObject;

public class GateVerification extends AppCompatActivity implements MqttCallback {
    private ProgressBar gateProgress;
    private int status = 0;
    boolean received = false;
    String messageReceived = "", userIdentity, sector;
    private Handler handler = new Handler();
    MqttClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gate_verification);


        final String userId = getIntent().getStringExtra("userId");
        final String sectorRec = getIntent().getStringExtra("sectorId");
        sector = sectorRec;
        userIdentity = userId;

        final String topicName = "IPB/SmartParking/1";

        gateProgress = findViewById(R.id.gateVerification);
        MemoryPersistence persistence = new MemoryPersistence();
        try {
            client = new MqttClient("tcp://193.136.195.56:1883", MqttClient.generateClientId(), persistence);
            MqttConnectOptions connectOptions = new MqttConnectOptions();
            connectOptions.setCleanSession(true);
            client.connect();
            client.setCallback(this);
            client.subscribe(topicName,2);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void connectionLost(Throwable cause) {

    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        messageReceived = message.toString();
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(status<5){
                    if(!messageReceived.isEmpty()){
                        if(received == false)
                            status++;
                    }
                    SystemClock.sleep(1);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            gateProgress.setProgress(status);
                        }
                    });
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        String message = messageReceived;
                        received = true;
                        status = 0;
                        Intent intent = new Intent(GateVerification.this, SpotSuccess.class);
                        intent.putExtra("gateMsg", message);
                        intent.putExtra("userId", userIdentity);
                        intent.putExtra("sectorId1", sector);
                        startActivity(intent);
                        finish();
                    }
                });
            }
        }).start();
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
