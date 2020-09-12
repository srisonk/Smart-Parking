package com.example.srisonkadariya.smartparking;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.design.widget.TextInputLayout;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
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
import net.fortuna.ical4j.model.property.Version;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.Calendar;
import java.util.List;

public class Reservation extends Activity implements OnItemSelectedListener {
    SQLiteDatabase db;
    SQLiteOpenHelper openHelper;
    Spinner location_spinner, profile_spinner;
    Button btnFinalizeReserve;
    DatePicker selected_date, end_date;
    TimePicker start_time, end_time;
    RadioButton rdbLoc, rdbPrice, rdbNone, rdbCar, rdbBike;
    Cursor cursor;
    TextInputLayout _txtPayAmount, txtParkRange, txtSpotNumber;
    String userProfile, labelLocation, latitude, longitude, priceWeight, distanceWeight, userId, calendarData,
            rdbVehicle;
    int locationid, distanceConverter;
    JSONObject obj = new JSONObject();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        openHelper = new UserDB(this);
        db = openHelper.getReadableDatabase();

        userId = getIntent().getStringExtra("idRec");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservation);

        location_spinner = findViewById(R.id.location_dropdown);
        profile_spinner = findViewById(R.id.profile_type_dropdown);

        start_time = findViewById(R.id.start_time);
        end_time = findViewById(R.id.end_time);

        btnFinalizeReserve = findViewById(R.id.btn_finalize_res);

        _txtPayAmount = findViewById(R.id.txtPayAmount);
        txtParkRange = findViewById(R.id.txtParkingRange);
        txtSpotNumber = findViewById(R.id.txtSpotNumber);


        location_spinner.setOnItemSelectedListener(this);
        loadSpinnerData();

        profile_spinner.setOnItemSelectedListener(this);
        loadSpotsTypeData();

        rdbLoc = findViewById(R.id.rdbLoc);
        rdbPrice = findViewById(R.id.rdbPri);
        rdbNone = findViewById(R.id.rdbNone);
        rdbCar = findViewById(R.id.rdbCar);
        rdbBike = findViewById(R.id.rdbBike);

        rdbPrice.setChecked(true);

        // The lower 5 lines are added so that only days starting from present moment can be selected from UI
        long now = System.currentTimeMillis() - 1000;
        selected_date = findViewById(R.id.date_picker);
        selected_date.setMinDate(now);

        end_date = findViewById(R.id.end_date_picker);
        end_date.setMinDate(now);



        btnFinalizeReserve.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                String payAmount = _txtPayAmount.getEditText().getText().toString().trim();
                String parkDist = txtParkRange.getEditText().getText().toString().trim();
                String spotNum = txtSpotNumber.getEditText().getText().toString().trim();
                if(payAmount.isEmpty() || parkDist.isEmpty() || spotNum.isEmpty()){
                    Toast.makeText(getApplicationContext(), "Please fill in all the fields!", Toast.LENGTH_LONG).show();
                }
                else if(Integer.parseInt(payAmount) < 1 || Integer.parseInt(payAmount) > 15 || rdbLoc == null )
                {
                    Toast.makeText(getApplicationContext(), "Please enter the price within the range mentioned!", Toast.LENGTH_LONG).show();
                }else if(Integer.parseInt(parkDist) < 0 || Integer.parseInt(parkDist) > 1000){
                    Toast.makeText(getApplicationContext(), "Please enter the distance range from 0 to 1000", Toast.LENGTH_LONG).show();
                }else if(Integer.parseInt(spotNum) < 1 || Integer.parseInt(spotNum) > 6){
                    Toast.makeText(getApplicationContext(), "Please enter the spot number from 1 to 6", Toast.LENGTH_LONG).show();
                }else if(!rdbCar.isChecked() && !rdbBike.isChecked()){
                    Toast.makeText(getApplicationContext(), "Please select a vehicle that you would like to park", Toast.LENGTH_LONG).show();
                }
                else
                    {
                        String localSpinner = profile_spinner.getSelectedItem().toString();

                        if(localSpinner.equals("Low")){
                            userProfile = "Conservative";
                        }else if(localSpinner.equals("Medium")){
                            userProfile = "Moderated";
                        }else if(localSpinner.equals("High")){
                            userProfile = "Aggressive";
                        }

                        if(Integer.parseInt(parkDist) >= 0 && Integer.parseInt(parkDist) <= 250){
                            distanceConverter = 1;
                        }else if(Integer.parseInt(parkDist) > 250 && Integer.parseInt(parkDist) <= 500){
                            distanceConverter = 2;
                        }else if(Integer.parseInt(parkDist) > 500 && Integer.parseInt(parkDist) <= 750){
                            distanceConverter = 3;
                        }else if(Integer.parseInt(parkDist) > 750 && Integer.parseInt(parkDist) <= 1000){
                            distanceConverter = 2;
                        }

                        if(rdbPrice.isChecked()){
                            priceWeight = "60";
                            distanceWeight = "40";
                        }else if(rdbLoc.isChecked()){
                            priceWeight = "40";
                            distanceWeight = "60";
                        }else if(rdbNone.isChecked()){
                            priceWeight = "50";
                            distanceWeight = "50";
                        }

                        if(rdbBike.isChecked()){
                            rdbVehicle = "bicycle";
                        }else if(rdbCar.isChecked()){
                            rdbVehicle = "car";
                        }

                        final int day = selected_date.getDayOfMonth();
                        final int month = selected_date.getMonth();
                        final int year = selected_date.getYear();

                        final int end_day = end_date.getDayOfMonth();
                        final int end_month = end_date.getMonth();
                        final int end_year = end_date.getYear();

                        final int hour = start_time.getHour();
                        final int minute = start_time.getMinute();

                        final int hour_end = end_time.getHour();
                        final int minute_end = end_time.getMinute();

                        String calFile = "firstClientEvent.ics";
                        String location = location_spinner.getSelectedItem().toString();
                        cursor = db.rawQuery("SELECT * FROM labels WHERE name = ?", new String[]{location});
                        if(cursor != null){
                            if(cursor.getCount() > 0){
                                if(cursor.moveToFirst()){
                                    latitude = cursor.getString(2);
                                    longitude = cursor.getString(3);
                                    locationid = cursor.getInt(0);
                                }
                            }
                        }

                        //Creating a new calendar
                        net.fortuna.ical4j.model.Calendar myCalendar = new net.fortuna.ical4j.model.Calendar();
                        myCalendar.getProperties().add(new ProdId("-//Srison kadariya//iCal4j 1.0//EN"));
                        myCalendar.getProperties().add(Version.VERSION_2_0);
                        myCalendar.getProperties().add(CalScale.GREGORIAN);

                        // Creating event with start date
                        Calendar startDate = Calendar.getInstance();
                        startDate.set(Calendar.MONTH, month);
                        startDate.set(Calendar.YEAR, year);
                        startDate.set(Calendar.DAY_OF_MONTH, day);
                        startDate.set(Calendar.HOUR_OF_DAY, hour);
                        startDate.set(Calendar.MINUTE, minute);
                        startDate.set(Calendar.SECOND, 0);

                        // Creating event with end date
                        Calendar endDate = Calendar.getInstance();
                        endDate.set(Calendar.MONTH, end_month);
                        endDate.set(Calendar.YEAR, end_year);
                        endDate.set(Calendar.DAY_OF_MONTH, end_day);
                        endDate.set(Calendar.HOUR_OF_DAY, hour_end);
                        endDate.set(Calendar.MINUTE, minute_end);
                        endDate.set(Calendar.SECOND, 0);

                        // Create the event
                        PropertyList pl = new PropertyList();
                        pl.add(new DtStart(new DateTime(startDate.getTime())));
                        pl.add(new DtEnd(new DateTime(endDate.getTime())));
                        pl.add(new Uid(userId));
                        pl.add(new Description("An iCal4j generated strings for communication with spot agents"));
                        VEvent parking = new VEvent(pl);

                        /*String eventName = "Client 1 parking";
                        DateTime start = new DateTime(startDate.getTime());
                        DateTime end = new DateTime(endDate.getTime());
                        Summary summary = new Summary("Hello World!");
                        VEvent parking = new VEvent(start, end, eventName, summary);*/

                        myCalendar.getComponents().add(parking);

                        try {
                            obj.put("start_day",day);
                            obj.put("start_month",month);
                            obj.put("start_year",year);
                            obj.put("start_hour",hour);
                            obj.put("start_minute",minute);
                            obj.put("end_day",end_day);
                            obj.put("end_month",end_month);
                            obj.put("end_year",end_year);
                            obj.put("end_hour",hour_end);
                            obj.put("end_minute",minute_end);
                            obj.put("msg_id",1);
                            obj.put("maximum_price",payAmount);
                            obj.put("location_id",locationid);
                            obj.put("latitude",latitude);
                            obj.put("longitude",longitude);
                            obj.put("user_profile",userProfile);
                            obj.put("price_weight",priceWeight);
                            obj.put("distance_weight",distanceWeight);
                            obj.put("distance_range",distanceConverter);
                            obj.put("user_id",userId);
                            obj.put("spot_id",spotNum);
                            obj.put("driver_type",rdbVehicle);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        String JSONMsg = obj.toString();

                        // Saving an iCalendar file

                        FileOutputStream fout;
                        try{
                            fout = openFileOutput(calFile, Context.MODE_PRIVATE);
                            fout.write(myCalendar.toString().getBytes());
                            fout.close();
                        }catch(Exception e){
                            e.printStackTrace();
                        }

                        //MQTT part
                        String topic = "IPB/SmartParking/UI";

                        //String messageContent = myCalendar.toString();


                        calendarData = myCalendar.toString();

                        String messageContent = JSONMsg;

                        String broker = "tcp://193.136.195.56:1883";
                        MemoryPersistence persistence = new MemoryPersistence();
                        try {
                            MqttClient myClient = new MqttClient(broker, MqttClient.generateClientId(), persistence);
                            MqttConnectOptions connectOptions = new MqttConnectOptions();
                            connectOptions.setCleanSession(true);
                            myClient.connect(connectOptions);
                            MqttMessage msg = new MqttMessage(messageContent.getBytes());
                            msg.setQos(2);
                            myClient.publish(topic, msg);
                            myClient.setTimeToWait(200);
                            myClient.close();
                            myClient.disconnect();

                        } catch (MqttException e) {
                            e.printStackTrace();
                        }
                        Intent intent = new Intent(Reservation.this, LoadingScreen.class);
                        intent.putExtra("idReceived",userId);
                        intent.putExtra("calendarData",calendarData);
                        startActivity(intent);
                        finish();
                    }

                // MQTT Block ends here
            }
        });
    }

    /**
     * Function to load the spinner data from SQLite database
     * */
    private void loadSpinnerData() {
        // database handler
        UserDB db = new UserDB(getApplicationContext());
        // Spinner Drop down elements
        List<String> labels = db.getAllLabels();
        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, labels);
        // Drop down layout style - list view with radio button
        dataAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // attaching data adapter to spinner
        location_spinner.setAdapter(dataAdapter);
    }

    private void loadSpotsTypeData() {
        // database handler
        UserDB db = new UserDB(getApplicationContext());
        // Spinner Drop down elements
        List<String> labels = db.getAllSpots();
        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, labels);
        // Drop down layout style - list view with radio button
        dataAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // attaching data adapter to spinner
        profile_spinner.setAdapter(dataAdapter);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // On selecting a spinner item
        String label = parent.getItemAtPosition(position).toString();

        // Showing selected spinner item
        Toast.makeText(parent.getContext(), "You selected: " + label, Toast.LENGTH_LONG).show();
        labelLocation = label;

    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO Auto-generated method stub

    }
}