package com.example.srisonkadariya.smartparking;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ProgressBar;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.PropertyList;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.Location;
import net.fortuna.ical4j.model.property.Summary;
import net.fortuna.ical4j.model.property.Uid;

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

import java.io.IOException;
import java.io.StringReader;

public class LoadingScreenAvailability extends AppCompatActivity implements MqttCallback {

    private ProgressBar progressBarAvailable;
    private int status = 0;
    private Handler handler = new Handler();
    SQLiteDatabase db;
    SQLiteOpenHelper openHelper;
    String messageReceived = "",
            JSONCaptureAnswer,JSONCaptureMsgId,JSONCaptureUserId, JSONCaptureTicket, CalendarRec="", loc, id;
    MqttClient client;
    boolean received = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading_screen_availability);

        openHelper = new UserDB(this);
        db = openHelper.getReadableDatabase();

        progressBarAvailable = findViewById(R.id.statusBarAvailability);

        final String calendarData3 = getIntent().getStringExtra("calendarData3");
        final String locationId = getIntent().getStringExtra("locationId");
        final String userId = getIntent().getStringExtra("userId");

        //loc = locationId;
        CalendarRec = calendarData3;
        id = userId;
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
            progressBarAvailable.setProgress(status);

        }
        String messageReceived1 = messageReceived;
        received = true;
        status = 0;
        try {
            JSONObject json = new JSONObject(messageReceived1);
            JSONCaptureAnswer = json.get("answer").toString();
            JSONCaptureUserId = json.get("user_id").toString();
            JSONCaptureMsgId = json.get("msg_id").toString();
            JSONCaptureTicket = json.get("spot_id").toString();
            loc = json.get("location_id").toString();


            // try to send iCal here

            StringReader sin = new StringReader(CalendarRec);
            CalendarBuilder builder = new CalendarBuilder();
            try {
                String topicName = "IPB/SmartParking/"+JSONCaptureTicket;
                Calendar calendar = builder.build(sin);

                PropertyList pl = new PropertyList();
                pl.add(new Summary(topicName));
                pl.add(new Location(JSONCaptureTicket));
                VEvent parking = new VEvent(pl);
                calendar.getComponents().add(parking);
                String iCalSender = calendar.toString();
                if(JSONCaptureAnswer.equals("true")){
                    MQTTPart(iCalSender);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ParserException e) {
                e.printStackTrace();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Intent intent = new Intent(LoadingScreenAvailability.this, ReservationCompletion.class);
        intent.putExtra("finalAnswer", JSONCaptureAnswer);
        intent.putExtra("finalUserId", JSONCaptureUserId);
        intent.putExtra("finalMsgIg", JSONCaptureMsgId);
        intent.putExtra("finalTicketId", JSONCaptureTicket);
        intent.putExtra("calendarData4", CalendarRec);
        intent.putExtra("locationId1", loc);
        intent.putExtra("userId", id);
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

    private void MQTTPart(String message){
        //MQTT part
        String topic = "IPB/SmartParking/iCalendars";
        String broker = "tcp://193.136.195.56:1883";
        MemoryPersistence persistence = new MemoryPersistence();

        try {
            MqttClient myClient = new MqttClient(broker, MqttClient.generateClientId(), persistence);
            MqttConnectOptions connectOptions = new MqttConnectOptions();
            connectOptions.setCleanSession(true);
            myClient.connect(connectOptions);
            MqttMessage msg = new MqttMessage(message.getBytes());
            msg.setQos(2);
            myClient.publish(topic, msg);
            myClient.setTimeToWait(200);

        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}
