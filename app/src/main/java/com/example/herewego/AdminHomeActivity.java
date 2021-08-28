package com.example.herewego;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class AdminHomeActivity extends AppCompatActivity {
    Button addUserButton, addDriverButton, addBusButton, viewUsersButton, viewLogsButton;
    FirebaseAuth mFirebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adminhome);

        addUserButton = findViewById(R.id.adduserbutton);
        addDriverButton = findViewById(R.id.adddriverbutton);
        addBusButton = findViewById(R.id.addbusbutton);
        viewUsersButton = findViewById(R.id.viewusersbutton);
        viewLogsButton = findViewById(R.id.adminviewlogsbutton);

        addUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent inttoadminadduser = new Intent(AdminHomeActivity.this, AdminAddUserActivity.class);
                startActivity(inttoadminadduser);
            }
        });

        addDriverButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent inttoadminadddriver = new Intent(AdminHomeActivity.this, AdminAddDriverActivity.class);
                startActivity(inttoadminadddriver);
            }
        });

        addBusButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent inttoadminaddbus = new Intent(AdminHomeActivity.this, AdminAddBusActivity.class);
                startActivity(inttoadminaddbus);
            }
        });

        viewUsersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent inttoadminviewusers = new Intent(AdminHomeActivity.this, AdminViewUsersActivity.class);
                startActivity(inttoadminviewusers);
            }
        });

        viewLogsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent inttoadminviewlogs = new Intent(AdminHomeActivity.this, AdminViewLogsActivity.class);
                startActivity(inttoadminviewlogs);
            }
        });
    }

    //function to inflate the action bar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.user_menu_action_bar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            new AlertDialog.Builder(AdminHomeActivity.this)
                    .setTitle("Logout?")
                    // Specifying a listener allows you to take an action before dismissing the dialog.
                    // The dialog is automatically dismissed when a dialog button is clicked.
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            mFirebaseAuth = FirebaseAuth.getInstance();
                            mFirebaseAuth.signOut();
                            Intent inttologin = new Intent(AdminHomeActivity.this, ChooseTypeActivity.class);
                            startActivity(inttologin);
                        }
                    })
                    // A null listener allows the button to dismiss the dialog and take no further action.
                    .setNegativeButton("No", null)
                    .show();
        }
        return true;
    }
}
