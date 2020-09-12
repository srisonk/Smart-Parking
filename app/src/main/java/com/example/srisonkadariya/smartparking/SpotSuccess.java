package com.example.srisonkadariya.smartparking;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;

public class SpotSuccess extends AppCompatActivity {
    TextView spotText;
    Button homeButton;
    String userIdentification;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spot_success);

        final String result = getIntent().getStringExtra("gateMsg");
        final String userId = getIntent().getStringExtra("userId");
        final String areaSector = getIntent().getStringExtra("sectorId1");
        userIdentification = userId;

        spotText = findViewById(R.id.spotResultText);
        homeButton = findViewById(R.id.homeSpotBtn);

        if(result.equals("true")){
            spotText.setText("Thank you for parking with us.");
        }else {
            spotText.setText("Sorry, the gate cannot be opened at this moment.");
        }

        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SpotSuccess.this, UserSession.class);
                intent.putExtra("id",userId);
                startActivity(intent);
                finish();
            }
        });
    }
}
