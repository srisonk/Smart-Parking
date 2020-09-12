package com.example.srisonkadariya.smartparking;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ResultSpot extends AppCompatActivity {
    Button confirm_btn, cancel_btn, recharge_btn, home_btn;
    SQLiteDatabase db;
    SQLiteOpenHelper openHelper;
    Cursor cursor, cursor2;
    String userCredits, store, email;
    JSONObject obj = new JSONObject();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        openHelper = new UserDB(this);
        db = openHelper.getReadableDatabase();

        final String resultsReceived = getIntent().getStringExtra("results");
        final String idReceived = getIntent().getStringExtra("finalId");
        final String priceReceived = getIntent().getStringExtra("priceFinal");
        final String locationReceivedId = getIntent().getStringExtra("locationId");
        final String calendarData2 = getIntent().getStringExtra("calendarData1");
        final String spotId = getIntent().getStringExtra("spotId");

        final int location_id_db = Integer.parseInt(locationReceivedId);
        final int user_id_db = Integer.parseInt(idReceived);
        final double price_db = Double.parseDouble(priceReceived);

        setContentView(R.layout.activity_result_spot);
        TextView finalText;
        finalText = findViewById(R.id.resultsText);
        finalText.setText(resultsReceived);

        confirm_btn = findViewById(R.id.confirmBtn);
        cancel_btn = findViewById(R.id.cancelBtn);
        recharge_btn = findViewById(R.id.rechargeBtn);
        home_btn = findViewById(R.id.homeButton);

        if(spotId.equals("-1")){
            confirm_btn.setVisibility(View.INVISIBLE);
            cancel_btn.setVisibility(View.INVISIBLE);
            home_btn.setVisibility(View.VISIBLE);
        }

        cursor = db.rawQuery("SELECT * FROM registration WHERE id = ?", new String[]{idReceived});
        if(cursor != null) {
            if (cursor.getCount() > 0) {
                if (cursor.moveToFirst()) {
                    userCredits = cursor.getString(6);
                    email = cursor.getString(4);
                }
            }
        }

        confirm_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Double.parseDouble(priceReceived) > Double.parseDouble(userCredits)){
                    Toast.makeText(getApplicationContext(), "Your balance is insufficient! Please recharge!", Toast.LENGTH_LONG).show();
                    try {
                        obj.put("user_id",idReceived);
                        obj.put("answer",false);
                        obj.put("msg_id",3);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    String JSONMsg = obj.toString();

                    MQTTPart(JSONMsg);
                    recharge_btn.setVisibility(View.VISIBLE);
                }else{
                    // Do all those successful tasks and MQTT tasks here.

                    double deduction = Double.parseDouble(userCredits) - Double.parseDouble(priceReceived);
                    store = String.valueOf(deduction);
                    cursor2 = db.rawQuery("UPDATE registration SET Credits = ? WHERE id = ?", new String[]{store, idReceived});
                    cursor2.moveToFirst();
                    cursor2.close();

                    @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("yyyy-dd-MM HH:mm");
                    String date = sdf.format(new Date());

                    UserDB rdb = new UserDB(getApplicationContext());
                    rdb.insertHistories(location_id_db,price_db,user_id_db, date);

                    try {
                        obj.put("user_id",idReceived);
                        obj.put("answer",true);
                        obj.put("msg_id",3);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    String JSONMsg = obj.toString();

                    MQTTPart(JSONMsg);

                    Intent redirect = new Intent(ResultSpot.this, LoadingScreenAvailability.class);
                    redirect.putExtra("calendarData3",calendarData2);
                    redirect.putExtra("locationId",locationReceivedId);
                    redirect.putExtra("userId",idReceived);
                    startActivity(redirect);
                    finish();
                }
            }
        });

        recharge_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ResultSpot.this, Recharge.class);
                intent.putExtra("idRec",idReceived);
                startActivity(intent);
                finish();
            }
        });

        cancel_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    obj.put("user_id",idReceived);
                    obj.put("answer",false);
                    obj.put("msg_id",3);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                String JSONMsg = obj.toString();

                MQTTPart(JSONMsg);

                Intent intent = new Intent(ResultSpot.this, UserSession.class);
                intent.putExtra("email",email);
                intent.putExtra("credits",userCredits);
                intent.putExtra("id",idReceived);
                startActivity(intent);
                finish();
            }
        });

        home_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ResultSpot.this, UserSession.class);
                intent.putExtra("email",email);
                intent.putExtra("credits",userCredits);
                intent.putExtra("id",idReceived);
                startActivity(intent);
                finish();
            }
        });
    }

    private void MQTTPart(String message){
        //MQTT part
        String topic = "IPB/SmartParking/UI";
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
            myClient.close();
            myClient.disconnect();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}
