package com.example.srisonkadariya.smartparking;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.PropertyList;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.DtEnd;
import net.fortuna.ical4j.model.property.DtStart;
import net.fortuna.ical4j.model.property.Location;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Summary;
import net.fortuna.ical4j.model.property.Uid;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import static net.fortuna.ical4j.model.property.Version.VERSION_2_0;

public class ReceivedReservationHistory extends AppCompatActivity {
    ArrayList<String> selectedItems = new ArrayList<>();
    ArrayAdapter<String> adapter;
    ListView reservationList;
    SQLiteDatabase db;
    SQLiteOpenHelper openHelper;
    Cursor cursor;
    String locationName, gidII, calSender;
    Button btnHome, btnGate;
    String location_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_received_reservation_history);

        final String id = getIntent().getStringExtra("idReceived");
        gidII = id;

        openHelper = new UserDB(this);
        db = openHelper.getReadableDatabase();

        btnHome = findViewById(R.id.btnHomeAgentHistory);
        btnGate = findViewById(R.id.btnOpenGateAgentHistory);

        final Intent intent = getIntent();
        ArrayList<String> date_list;
        ArrayList<String> checkList = new ArrayList<>();
        date_list = intent.getStringArrayListExtra("date_list");
        String[] eventList = new String[date_list.size()];
        net.fortuna.ical4j.model.Calendar[] calendars = new net.fortuna.ical4j.model.Calendar[date_list.size()];


        for(int i=0; i<date_list.size();i++){
            String[] Dates;
            String[] StartDate = new String[date_list.size()];
            String[] EndDate = new String[date_list.size()];
            String[] user_id = new String[date_list.size()];
            String[] location_id = new String[date_list.size()];
            String[] spot_id = new String[date_list.size()];
            calendars[i] = new net.fortuna.ical4j.model.Calendar();
            eventList[i] = date_list.get(i);
            Dates = eventList[i].split("&");
            StartDate[i] = Dates[0];
            EndDate[i] = Dates[1];
            user_id[i] = Dates[2];
            spot_id[i] = Dates[3];
            location_id[i] = Dates[4];


            cursor = db.rawQuery("SELECT * FROM labels WHERE id = ?", new String[]{location_id[i]});
            if(cursor != null){
                if(cursor.getCount() > 0){
                    if(cursor.moveToFirst()){
                        locationName = cursor.getString(1);
                    }
                }
            }
            checkList.add(StartDate[i]+" at "+locationName+" , Spot Num: "+spot_id[i]);

            DateFormat dateFormat = new SimpleDateFormat(
                    "E MMM dd HH:mm:ss Z yyyy", Locale.UK);
            try {
                Date parkDate = dateFormat.parse(StartDate[i]);
                Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Europe/UK"));
                cal.setTime(parkDate);

                Date parkDateEnd = dateFormat.parse(EndDate[i]);
                Calendar calEnd = Calendar.getInstance(TimeZone.getTimeZone("Europe/UK"));
                calEnd.setTime(parkDateEnd);

                calendars[i].getProperties().add(new ProdId("\"-//Srison kadariya//iCal4j 1.0//EN\""));
                calendars[i].getProperties().add(VERSION_2_0);
                calendars[i].getProperties().add(CalScale.GREGORIAN);

                PropertyList pl = new PropertyList();

                pl.add(new DtStart(new DateTime(cal.getTime())));
                pl.add(new DtEnd(new DateTime(calEnd.getTime())));
                pl.add(new Uid(user_id[i]));
                pl.add(new Summary("IPB/SmartParking/1"));
                pl.add(new Description("An iCal4j generated strings during open gate states"));
                pl.add(new Location("1"));
                VEvent parking = new VEvent(pl);

                calendars[i].getComponents().add(parking);

            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        reservationList = findViewById(R.id.receivedReservationList);

        StringBuilder stringBuilder = new StringBuilder();
        for (net.fortuna.ical4j.model.Calendar calendar : calendars) {
            stringBuilder.append(calendar+"&");
        }
        calSender = stringBuilder.toString();

        MQTTPart("IPB/SmartParking/iCalendars",calSender);

        adapter = new ArrayAdapter<>(this, R.layout.rowlayout, checkList);

        reservationList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        reservationList.setAdapter(adapter);

        btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(ReceivedReservationHistory.this, UserSession.class);
                intent1.putExtra("id", gidII);
                startActivity(intent1);
                finish();
            }
        });

        reservationList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Object obj = reservationList.getItemAtPosition(position);
                String selectedItem = obj.toString();
                btnGate.setVisibility(View.VISIBLE);
                if(selectedItems.contains(selectedItem)){
                    selectedItems.remove(selectedItem);
                }else{
                    selectedItems.add(selectedItem);
                }
            }
        });

        btnGate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String items = "";
                for(String item:selectedItems){
                    items += "-"+item+"\n";
                }

                String[] ItemArr = items.split(" ");
                String location = ItemArr[7];
                cursor = db.rawQuery("SELECT * FROM labels WHERE name = ?",new String[]{location.replace("\n","")});
                if(cursor != null){
                    if(cursor.getCount() > 0){
                        if(cursor.moveToFirst()){
                            location_id = cursor.getString(0);
                        }
                    }
                }
                MQTTPart("IPB/SmartParking/Sector"+location_id,"open"+id);
                Intent intent = new Intent(ReceivedReservationHistory.this, LoadingOnlineGate.class);
                intent.putExtra("id", gidII);
                intent.putExtra("location_id", location_id);
                startActivity(intent);
                finish();
            }
        });

    }

    private void MQTTPart(String topic,String message){
        //MQTT part
        //String topic = "IPB/SmartParking/iCalendars";
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
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}
