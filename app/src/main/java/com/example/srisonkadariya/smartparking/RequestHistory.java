package com.example.srisonkadariya.smartparking;

import android.content.Intent;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ProgressBar;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public class RequestHistory extends AppCompatActivity implements MqttCallback {
    private ProgressBar progressBarAvailable;
    private int status = 0;
    private Handler handler = new Handler();
    String messageReceived = "", s1, s2, s3, s4, s5, gid;
    MqttClient client;
    boolean received = false;

    //Test
    ArrayList<String> JSON_Date = new ArrayList<>();
    ArrayList<String> MQTTMsgList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_history);

        final String id = getIntent().getStringExtra("idRec");

        gid = id;

        progressBarAvailable = findViewById(R.id.request_reservation_history_bar);

        MemoryPersistence persistence = new MemoryPersistence();
        try {
            client = new MqttClient("tcp://193.136.195.56:1883", MqttClient.generateClientId(), persistence);
            MqttConnectOptions connectOptions = new MqttConnectOptions();
            connectOptions.setCleanSession(true);
            client.connect();
            client.setCallback(this);
            client.subscribe("IPB/SmartParking/UI",2);
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
        MQTTMsgList.add(messageReceived);
        while(status<2){
            if(!messageReceived.isEmpty()){
                if(!received) {
                    status++;
                }
            }
            SystemClock.sleep(1);
            progressBarAvailable.setProgress(status);
        }
        received = true;
        if(MQTTMsgList.size() == 6){
            status = 0;
            this.messageProcessing();
        }
        try {
            client.close();
            client.disconnect();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }



    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        try {
            client.close();
            client.disconnect();
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void messageProcessing(){
        String[] messages = MQTTMsgList.toArray(new String[0]);
        for(int j = 0; j<messages.length; j++){
            String messageCollector = messages[j];
            try {
                JSONObject obj = new JSONObject(messageCollector);
                // Receive all of those JSON Strings here..

                s3 = obj.getString("user_id");
                s4 = obj.getString("spot_id");
                s5 = obj.getString("location_id");

                JSONArray arr = obj.getJSONArray("reservas");

                for(int i = 0; i < arr.length(); i++){
                    obj = arr.getJSONObject(i);
                    s1 = obj.getString("inicio");
                    s2 = obj.getString("fim");

                    JSON_Date.add(s1+"&"+s2+"&"+s3+"&"+s4+"&"+s5);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        Intent intent = new Intent(RequestHistory.this, ReceivedReservationHistory.class);
        intent.putStringArrayListExtra("date_list",JSON_Date);
        intent.putExtra("idReceived",gid);
        startActivity(intent);
        finish();
    }
}
