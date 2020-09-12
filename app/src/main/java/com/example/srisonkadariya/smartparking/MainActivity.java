package com.example.srisonkadariya.smartparking;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class MainActivity extends AppCompatActivity {
    SQLiteOpenHelper openHelper;
    SQLiteDatabase db;
    Button _btnreg, _btnlogin;
    TextInputLayout _txtname, _txtsurname, _txtemail, _txtpassword, _txtrptpassword, _txtphone;
    private static final String ALGORITHM = "AES";
    private static final String KEY = "1Hbfh667adfDEJ78";
    String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        openHelper = new UserDB(this);
        _btnreg = findViewById(R.id.btnreg);
        _btnlogin = findViewById(R.id.btnlogin);
        _txtname = findViewById(R.id.txtname);
        _txtsurname = findViewById(R.id.txtsurname);
        _txtemail = findViewById(R.id.txtemail);
        _txtpassword = findViewById(R.id.txtpassword);
        _txtrptpassword = findViewById(R.id.txtrepeatpassword);
        _txtphone = findViewById(R.id.txtphone);

        _btnreg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db = openHelper.getWritableDatabase();
                String name = _txtname.getEditText().getText().toString().trim();
                String surname = _txtsurname.getEditText().getText().toString().trim();
                String email = _txtemail.getEditText().getText().toString().trim();
                String normalPassword = _txtpassword.getEditText().getText().toString().trim();
                String repeatPassword = _txtrptpassword.getEditText().getText().toString().trim();
                try {
                    password = encrypt(normalPassword);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                String phone = _txtphone.getEditText().getText().toString().trim();
                Double credits = 0.0;

                if(name.isEmpty() || surname.isEmpty() || email.isEmpty() || password.isEmpty() || phone.isEmpty()) {
                    Toast toast = Toast.makeText(getApplicationContext(), "Error: Please fill all the fields", Toast.LENGTH_LONG);
                    View view = toast.getView();
                    TextView text =  view.findViewById(android.R.id.message);
                    text.setTextColor(Color.parseColor("#ff9494"));
                    toast.show();
                }
                else if(!normalPassword.equals(repeatPassword)){
                    Toast toast = Toast.makeText(getApplicationContext(), "Error: Given passwords do not match!", Toast.LENGTH_LONG);
                    View view = toast.getView();
                    TextView text =  view.findViewById(android.R.id.message);
                    text.setTextColor(Color.parseColor("#ff9494"));
                    toast.show();
                }
                else{
                    insertdata(name, surname, email, password, phone, credits);
                    Toast.makeText(getApplicationContext(), "Registration successful!", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(MainActivity.this, login.class);
                    startActivity(intent);
                }
            }
        });

        _btnlogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, login.class);
                startActivity(intent);
            }
        });

    }

    public void insertdata(String name, String surname, String email, String password, String phone, Double credits){
        ContentValues contentValues = new ContentValues();
        contentValues.put(UserDB.COL_2, name);
        contentValues.put(UserDB.COL_3, surname);
        contentValues.put(UserDB.COL_4, email);
        contentValues.put(UserDB.COL_5, password);
        contentValues.put(UserDB.COL_6, phone);
        contentValues.put(UserDB.COL_7, credits);
        long id = db.insert(UserDB.TABLE_NAME, null, contentValues);
    }

    public static String encrypt(String value) throws Exception
    {
        Key key = generateKey();
        Cipher cipher = Cipher.getInstance(MainActivity.ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte [] encryptedByteValue = cipher.doFinal(value.getBytes("utf-8"));
        String encryptedValue64 = Base64.encodeToString(encryptedByteValue, Base64.DEFAULT);
        return encryptedValue64;

    }

    private static Key generateKey() throws Exception
    {
        Key key = new SecretKeySpec(MainActivity.KEY.getBytes(),MainActivity.ALGORITHM);
        return key;
    }
}
