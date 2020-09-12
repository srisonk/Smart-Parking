package com.example.srisonkadariya.smartparking;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class Recharge extends AppCompatActivity {
    SQLiteDatabase db;
    SQLiteOpenHelper openHelper;
    TextInputLayout _txtCredits;
    Button _btnAdd, _btnHome;
    Cursor cursor, cursor1, pageCursor;
    String totalConverted, pageCredits, email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recharge);

        openHelper = new UserDB(this);
        db = openHelper.getReadableDatabase();

        final String id = getIntent().getStringExtra("idRec");

        pageCursor = db.rawQuery("SELECT * FROM registration WHERE id = ?", new String[]{id});
        if(pageCursor != null){
            if(pageCursor.getCount() > 0){
                if(pageCursor.moveToFirst()){
                    pageCredits = pageCursor.getString(6);
                }
            }
        }

        _btnAdd = findViewById(R.id.btnAddCredits);
        _btnHome = findViewById(R.id.btnHome);
        _txtCredits = findViewById(R.id.txtAmount);

        TextView tv = findViewById(R.id.reservation_text);
        tv.setText(pageCredits);

        _btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String amount = _txtCredits.getEditText().getText().toString().trim();

                if(amount.isEmpty()){
                    Toast.makeText(getApplicationContext(), "Please provide some amount!", Toast.LENGTH_LONG).show();
                }else{
                    cursor1 = db.rawQuery("SELECT * FROM registration WHERE id = ?", new String[]{id});
                    if (cursor1 != null) {
                        if (cursor1.getCount() > 0) {
                            if (cursor1.moveToFirst()) {
                                String leftover = cursor1.getString(6);
                                email = cursor1.getString(4);
                                Double leftoverConverter = Double.parseDouble(leftover);
                                Double amountConverter = Double.parseDouble(amount);
                                Double total = leftoverConverter + amountConverter;
                                totalConverted = String.valueOf(total);
                            }
                        }
                    }
                    cursor = db.rawQuery("UPDATE registration SET Credits = ? WHERE id = ?", new String[]{totalConverted, id});
                    Toast.makeText(getApplicationContext(), "€"+amount+" is successfully added!", Toast.LENGTH_LONG).show();
                    cursor.moveToFirst();
                    cursor.close();
                    finish();
                    startActivity(getIntent());
                }
            }
        });

        _btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Recharge.this, UserSession.class);
                intent.putExtra("email",email);
                intent.putExtra("credits",pageCredits);
                intent.putExtra("id",id);
                startActivity(intent);
                finish();
            }
        });
    }
}
