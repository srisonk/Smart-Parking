package com.example.srisonkadariya.smartparking;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class UserDB extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "smartparking.db";
    public static final String TABLE_NAME = "registration";
    public static final String COL_2 = "Name";
    public static final String COL_3 = "Surname";
    public static final String COL_4 = "Email";
    public static final String COL_5 = "Password";
    public static final String COL_6 = "Phone";
    public static final String COL_7 = "Credits";

    private static final String TABLE_LABELS = "labels";
    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String LAT = "latitude";
    private static final String LONG = "longitude";

    // Table for type of parking spot
    private static final String table_profile_type = "profile_type";
    private static final String type_id = "id";
    private static final String type_name = "name";

    // Test table for History
    private static final String HISTORY_TABLE_LABELS = "history";
    private static final String HISTORY_KEY_ID = "id";
    private static final String HISTORY_LOCATION_ID = "location_id";
    private static final String HISTORY_PRICE = "price";
    private static final String HISTORY_USER_ID = "user_id";
    private static final String HISTORY_DATE = "history_date";


    public UserDB(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_NAME + "(ID INTEGER PRIMARY KEY AUTOINCREMENT,Name TEXT,Surname TEXT,Email TEXT,Password TEXT,Phone NUMBER, Credits REAL)");
        String CREATE_CATEGORIES_TABLE = "CREATE TABLE " + TABLE_LABELS + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_NAME + " TEXT,"+ LAT+" TEXT,"+LONG+" TEXT)";

        // parking type table creation..
        String CREATE_PARK_TYPE_TABLE = "CREATE TABLE " + table_profile_type + "("
                + type_id + " INTEGER PRIMARY KEY," + type_name + " TEXT)";

        // Test History table
        String CREATE_HISTORY_TABLE = "CREATE TABLE " + HISTORY_TABLE_LABELS + "("
                + HISTORY_KEY_ID + " INTEGER PRIMARY KEY," + HISTORY_LOCATION_ID + "" +
                " INTEGER,"+ HISTORY_PRICE+" REAL,"+HISTORY_USER_ID+" INTEGER,"+HISTORY_DATE+" TEXT)";

        db.execSQL(CREATE_CATEGORIES_TABLE);
        db.execSQL(CREATE_PARK_TYPE_TABLE);
        db.execSQL(CREATE_HISTORY_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS '"+TABLE_NAME+"'");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LABELS);
        db.execSQL("DROP TABLE IF EXISTS " + table_profile_type);
        db.execSQL("DROP TABLE IF EXISTS " + HISTORY_TABLE_LABELS);
        onCreate(db);
    }

    public void insertLabel(String label, String latitude, String longitude){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_NAME, label);
        values.put(LAT, latitude);
        values.put(LONG, longitude);
        db.insert(TABLE_LABELS, null, values);
        db.close();
    }

    public void insertProfileType(String label){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(type_name, label);
        db.insert(table_profile_type, null, values);
        db.close();
    }

    public void insertHistories(int location_id, double price, int user_id, String history_date){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(HISTORY_LOCATION_ID, location_id);
        values.put(HISTORY_PRICE, price);
        values.put(HISTORY_USER_ID, user_id);
        values.put(HISTORY_DATE, history_date);
        db.insert(HISTORY_TABLE_LABELS, null, values);
        db.close();
    }

    public List<String> getAllLabels(){
        List<String> labels = new ArrayList<>();
        String selectQuery = "SELECT  * FROM " + TABLE_LABELS;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                labels.add(cursor.getString(1));
            } while (cursor.moveToNext());
        }

        // closing connection
        cursor.close();
        db.close();

        // returning label
        return labels;
    }

    public List<String> getAllSpots(){
        List<String> labels = new ArrayList<>();
        String selectQuery = "SELECT  * FROM " + table_profile_type;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                labels.add(cursor.getString(1));
            } while (cursor.moveToNext());
        }

        // closing connection
        cursor.close();
        db.close();

        // returning label
        return labels;
    }

}
