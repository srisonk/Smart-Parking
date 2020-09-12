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

public class LoadingOnlineGate extends AppCompatActivity implements MqttCallback {

    private ProgressBar progressBar;
    private int status = 0;
    private Handler handler = new Handler();
    String messageReceived = "",id, location_id;
    MqttClient client;
    boolean received = false;

    public LoadingOnlineGate(){}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading_online_gate);

        id = getIntent().getStringExtra("id");

        location_id = getIntent().getStringExtra("location_id");

        progressBar = findViewById(R.id.onlineGateBar);

        MemoryPersistence persistence = new MemoryPersistence();
        try {
            client = new MqttClient("tcp://193.136.195.56:1883", MqttClient.generateClientId(), persistence);
            MqttConnectOptions connectOptions = new MqttConnectOptions();
            connectOptions.setCleanSession(true);
            client.connect();
            client.setCallback(this);
            client.subscribe("IPB/SmartParking/"+id,2);
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
        while(status<5){
            if(!messageReceived.isEmpty()){
                if(!received) {
                    status++;
                }
            }
            SystemClock.sleep(1);
            progressBar.setProgress(status);
        }
        String messageReceived1 = messageReceived;
        received = true;
        status = 0;
        Intent intent = new Intent(LoadingOnlineGate.this, SpotSuccess.class);
        intent.putExtra("gateMsg", messageReceived1);
        intent.putExtra("userId",id);
        startActivity(intent);
        finish();
    }



    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {

    }
}
