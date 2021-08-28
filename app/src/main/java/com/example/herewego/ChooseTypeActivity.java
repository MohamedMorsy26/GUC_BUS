package com.example.herewego;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

public class ChooseTypeActivity extends AppCompatActivity {
    ImageButton driverButton, passengerButton;
    Button adminButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choosetype);

        driverButton = findViewById(R.id.driverbutton);
        passengerButton = findViewById(R.id.passengerbutton);
        adminButton = findViewById(R.id.adminbutton);

        driverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent inttodriver = new Intent(ChooseTypeActivity.this, DriverLoginActivity.class);
                startActivity(inttodriver);
            }
        });

        passengerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent inttopass = new Intent(ChooseTypeActivity.this, UserLoginActivity.class);
                startActivity(inttopass);
            }
        });
        adminButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent inttoadmin = new Intent(ChooseTypeActivity.this, AdminLoginActivity.class);
                startActivity(inttoadmin);
            }
        });

    }

}
