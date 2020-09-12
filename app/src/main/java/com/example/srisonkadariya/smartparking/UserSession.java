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
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.JSONException;
import org.json.JSONObject;

public class UserSession extends AppCompatActivity {
    SQLiteDatabase db;
    SQLiteOpenHelper openHelper;
    Cursor cursor;
    private SessionManager session;

    String credits, name, location_id = null;

    public String getCredits() {
        return credits;
    }
    public void setCredits(String credits) {
        this.credits = credits;
    }

    String id;
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_session);

        openHelper = new UserDB(this);
        db = openHelper.getReadableDatabase();

        setId(getIntent().getStringExtra("id"));

        cursor = db.rawQuery("SELECT * FROM registration WHERE id = ?", new String[]{id});
        if(cursor != null){
            if(cursor.getCount() > 0){
                if(cursor.moveToFirst()){
                    name = cursor.getString(1);
                }
            }
        }

        Button _btnreserve = findViewById(R.id.btnReserve);
        Button _btnhistory = findViewById(R.id.btnHistory);
        Button _btnViewResv = findViewById(R.id.btnViewReservation);

        TextView textViewName = findViewById(R.id.welcomeText);
        textViewName.setText("Welcome "+name);

        setCredits(getIntent().getStringExtra("credits"));

        session = new SessionManager(this);
        if (!session.loggedin()) {
            logout();
        }
        Button _btnlogout = findViewById(R.id.btnlogout);

        _btnlogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });

        Button _btncredits = findViewById(R.id.btncredits);

        _btncredits.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                credits();
            }
        });


        _btnreserve.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reservation();
                reservationType();
            }
        });

        _btnhistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserSession.this, History.class);
                intent.putExtra("userId",getId());
                startActivity(intent);
            }
        });

        _btnViewResv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cursor = db.rawQuery("SELECT * FROM labels where id = ?",new String[]{"1"});
                if(cursor != null){
                    if(cursor.getCount() > 0){
                        requestHistory();
                    }else{
                        Toast.makeText(getApplicationContext(), "No any reservations to show yet!", Toast.LENGTH_LONG).show();
                    }
                }

            }
        });
    }

    public void reservation(){
        startActivity(new Intent(UserSession.this, Reservation.class));

        // The lower code(71-77) was added later so that user can select the location instead of typing it..
        UserDB db = new UserDB(getApplicationContext());
        //db.deleteAll();
        if(db.getAllLabels().isEmpty()){
            db.insertLabel("ESTiG", "41.796443", "-6.768707");
            db.insertLabel("Agraria", "41.797545", "-6.767433");
            db.insertLabel("Cantina IPB", "41.799090", "-6.760456");
            db.insertLabel("Avenida Sá Carneiro", "41.803173", "-6.768707");
            db.insertLabel("CTT", "41.807254", "-6.759178");
        }
        // The added code ends here..
    }

    private void reservationType(){
        Intent intent = new Intent(UserSession.this, Reservation.class);
        UserDB db = new UserDB(getApplicationContext());
        if(db.getAllSpots().isEmpty()){
            db.insertProfileType("Low");
            db.insertProfileType("Medium");
            db.insertProfileType("High");
        }
        intent.putExtra("idRec",getId());
        startActivity(intent);
    }

    private void credits(){
        Intent intent = new Intent(UserSession.this,Recharge.class);
        intent.putExtra("creditsRec",getCredits());
        intent.putExtra("idRec",getId());
        startActivity(intent);

    }

    private void logout() {
        session.setLoggedin(false);
        finish();
        startActivity(new Intent(UserSession.this, login.class));
    }

    private void requestHistory(){
        JSONObject obj = new JSONObject();
        try {
            obj.put("msg_id",5);
            obj.put("user_id",id);
            obj.put("location_id",location_id);
            String JSONMsg = obj.toString();
            MQTTPart(JSONMsg);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Intent intent = new Intent(UserSession.this, RequestHistory.class);
        intent.putExtra("idRec",getId());
        startActivity(intent);
        finish();
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


        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}