package com.example.srisonkadariya.smartparking;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.property.Location;
import net.fortuna.ical4j.model.property.Summary;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.io.IOException;
import java.io.StringReader;

public class ReservationCompletion extends AppCompatActivity {
    TextView finalText, ticket, gate;
    Button finalButton, gateButton, mapButton;
    SQLiteDatabase db;
    SQLiteOpenHelper openHelper;
    Cursor cursor;
    String email, id, sectorNumber, topicName;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservation_completion);

        openHelper = new UserDB(this);
        db = openHelper.getReadableDatabase();

        final String finalAnswer = getIntent().getStringExtra("finalAnswer");
        final String finalUserId = getIntent().getStringExtra("finalUserId");
        final String finalTicketId = getIntent().getStringExtra("finalTicketId");
        final String calendarData4 = getIntent().getStringExtra("calendarData4");
        final String location_id = getIntent().getStringExtra("locationId1");
        sectorNumber = location_id;
        final String user_id = getIntent().getStringExtra("userId");
        id = user_id;

        finalText = findViewById(R.id.txtCompletion);
        ticket = findViewById(R.id.txtTicketNum);
        finalButton = findViewById(R.id.finalHomeBtn);
        gate = findViewById(R.id.txtGate);
        gateButton = findViewById(R.id.gateBtn);
        mapButton = findViewById(R.id.mapsBtn);

        if(finalAnswer.equals("true")){
            finalText.setText("Dear customer, your reservation has succeeded");
            ticket.setVisibility(TextView.VISIBLE);
            ticket.setText("Your spot number is "+finalTicketId);
            gate.setVisibility(TextView.VISIBLE);
            gateButton.setVisibility(Button.VISIBLE);
            mapButton.setVisibility(Button.VISIBLE);
        }
        else{
            finalText.setText("We're sorry but someone already took the spot");
            finalButton.setVisibility(Button.VISIBLE);
        }

        finalButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cursor = db.rawQuery("SELECT * FROM registration WHERE ID = ?", new String[]{finalUserId});
                if(cursor != null){
                    if(cursor.getCount() > 0){
                        if(cursor.moveToFirst()){
                            email = cursor.getString(3);
                        }
                    }
                }

                Intent intent = new Intent(ReservationCompletion.this, UserSession.class);
                intent.putExtra("id",finalUserId);
                intent.putExtra("email",email);
                startActivity(intent);
                finish();
            }
        });

        gateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MQTTPart("open"+user_id);
                Intent intent = new Intent(ReservationCompletion.this, GateVerification.class);
                intent.putExtra("userId",id);
                intent.putExtra("sectorId",location_id);
                startActivity(intent);
                finish();
            }
        });

        mapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String lat="",lng="";
                cursor = db.rawQuery("SELECT * FROM labels WHERE id = ?",new String[]{location_id});
                if(cursor != null){
                    if(cursor.getCount() > 0){
                        if(cursor.moveToFirst()){
                            lat = cursor.getString(2);
                            lng = cursor.getString(3);
                        }
                    }
                }
                Uri googleMapsURI = Uri.parse("geo:"+lat+","+lng+"?q="+lat+","+lng);
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, googleMapsURI);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);
            }
        });
    }

    private void MQTTPart(String message){
        //MQTT part
        String topic = "IPB/SmartParking/Sector"+sectorNumber;
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
