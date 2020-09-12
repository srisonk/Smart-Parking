package com.example.srisonkadariya.smartparking;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
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


public class LoadingScreen extends AppCompatActivity implements MqttCallback {

    private ProgressBar progressBar;
    private int status = 0;
    private Handler handler = new Handler();
    SQLiteDatabase db;
    SQLiteOpenHelper openHelper;
    Cursor cursor;
    String location, stringSender, userId, messageReceived = "",
            JSONCapturePrice,JSONCaptureMsgId,JSONCaptureLocation,JSONCaptureSpotId, calendarData1;
    MqttClient client;
    boolean received = false;

    public LoadingScreen(){}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        openHelper = new UserDB(this);
        db = openHelper.getReadableDatabase();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading_screen);

        progressBar = findViewById(R.id.statusBar);
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
        while(status<5){
            if(!messageReceived.isEmpty()){
                if(!received) {
                    status++;
                }
            }
            SystemClock.sleep(1);
            progressBar.setProgress(status);
        }
        received = true;
        status = 0;
        try {
            JSONObject json = new JSONObject(messageReceived);
            JSONCapturePrice = json.get("price").toString();
            JSONCaptureLocation = json.get("location_id").toString();
            JSONCaptureMsgId = json.get("msg_id").toString();
            JSONCaptureSpotId = json.get("spot_id").toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        cursor = db.rawQuery("SELECT * FROM labels WHERE id = ?", new String[]{JSONCaptureLocation});
        if(cursor != null){
            if(cursor.getCount() > 0){
                if(cursor.moveToFirst()){
                    location = cursor.getString(1);
                }
            }
        }
        if(JSONCaptureSpotId.equals("-1")){
            stringSender = "We're sorry. There are no spots currently available at "+location;
        }
        else{
            stringSender = "We recommend you spot number "+JSONCaptureSpotId+" at "+location+" with price of €"+JSONCapturePrice;
        }
        userId = getIntent().getStringExtra("idReceived");
        calendarData1 = getIntent().getStringExtra("calendarData");
        Intent intent = new Intent(LoadingScreen.this, ResultSpot.class);
        intent.putExtra("results", stringSender);
        intent.putExtra("priceFinal", JSONCapturePrice);
        intent.putExtra("finalId", userId);
        intent.putExtra("locationId", JSONCaptureLocation);
        intent.putExtra("calendarData1", calendarData1);
        intent.putExtra("spotId", JSONCaptureSpotId);
        startActivity(intent);
        finish();
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
