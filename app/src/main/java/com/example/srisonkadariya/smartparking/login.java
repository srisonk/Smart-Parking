package com.example.srisonkadariya.smartparking;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.database.sqlite.SQLiteDatabase;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class login extends AppCompatActivity {
    SQLiteDatabase db;
    SQLiteOpenHelper openHelper;
    Button _btnLogin, _btnRegistration;
    TextInputLayout _txtEmail, _txtPassword;
    Cursor cursor;
    private SessionManager session;
    private static final String ALGORITHM = "AES";
    private static final String KEY = "1Hbfh667adfDEJ78";

    String credits, id, AESPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        openHelper = new UserDB(this);
        db = openHelper.getReadableDatabase();
        _btnRegistration = findViewById(R.id.btnRegistration);
        _btnLogin = findViewById(R.id.btnLogin);
        _txtEmail =  findViewById(R.id.txtEmail);
        _txtPassword = findViewById(R.id.txtPassword);


        session = new SessionManager(this);
        if (session.loggedin()){
            Intent intent = new Intent(login.this, UserSession.class);
            startActivity(intent);
            finish();
        }
        _btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = _txtEmail.getEditText().getText().toString().trim();
                String normalPassword = _txtPassword.getEditText().getText().toString().trim();
                try {
                    AESPassword = encrypt(normalPassword);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                cursor = db.rawQuery("SELECT * FROM registration WHERE Email =? AND Password =?", new String[]{email, AESPassword});
                if(cursor != null){
                    if(cursor.getCount() > 0){

                        if(cursor.moveToFirst()){
                            credits = cursor.getString(6);
                            id = cursor.getString(0);
                        }

                        session.setLoggedin(true);
                        Toast.makeText(getApplicationContext(), "Login Success" , Toast.LENGTH_LONG).show();

                        Intent intent = new Intent(login.this, UserSession.class);
                        intent.putExtra("email",email);
                        intent.putExtra("credits",credits);
                        intent.putExtra("id",id);
                        startActivity(intent);
                        finish();
                    }
                    else if(email.isEmpty() || normalPassword.isEmpty()){
                        Toast.makeText(getApplicationContext(), "Please provide the credentials", Toast.LENGTH_LONG).show();
                    }
                    else{
                        Toast.makeText(getApplicationContext(), "Error: Please recheck username or password", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

        _btnRegistration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(login.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    public static String encrypt(String value) throws Exception
    {
        Key key = generateKey();
        Cipher cipher = Cipher.getInstance(login.ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte [] encryptedByteValue = cipher.doFinal(value.getBytes("utf-8"));
        String encryptedValue64 = Base64.encodeToString(encryptedByteValue, Base64.DEFAULT);
        return encryptedValue64;
    }

    private static Key generateKey() throws Exception
    {
        Key key = new SecretKeySpec(login.KEY.getBytes(),login.ALGORITHM);
        return key;
    }
}
