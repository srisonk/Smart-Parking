package com.example.srisonkadariya.smartparking;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import java.util.ArrayList;

public class History extends AppCompatActivity {
    ArrayList<String> listItem;
    ArrayAdapter<String> adapter;
    Cursor cursor;
    SQLiteDatabase db;
    SQLiteOpenHelper openHelper;
    TextView _noHistory;
    String user_name;

    ListView historyList;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        _noHistory = findViewById(R.id.noHistory);

        final String id = getIntent().getStringExtra("userId");

        historyList = findViewById(R.id.historyList);

        ArrayList<String> checkList = new ArrayList<>();
        listItem = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, checkList);

        historyList.setAdapter(adapter);

        openHelper = new UserDB(this);
        db = openHelper.getReadableDatabase();

        cursor = db.rawQuery("SELECT * FROM registration WHERE ID = ?", new String[]{id});
        if(cursor != null){
            if(cursor.getCount() > 0){
                if(cursor.moveToFirst()){
                    user_name = cursor.getString(1);
                }
            }
        }

        _noHistory.setText(user_name + "´s reservation history");

        cursor = db.rawQuery("SELECT h.id, h.location_id, h.price, h.user_id, h.history_date, l.name, r.name from history h " +
                "INNER JOIN labels l on l.id = h.location_id " +
                "INNER JOIN registration r on r.ID = h.user_id " +
                "WHERE h.user_id = ?", new String[]{id});

        if (cursor.moveToFirst()) {
            do {
                adapter.add(cursor.getString(4)+": At "+cursor.getString(5)+" with €"+cursor.getDouble(2));
            } while (cursor.moveToNext());
        }
    }
}
