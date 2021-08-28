package com.example.herewego;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AdminAddBusActivity extends AppCompatActivity {
    EditText numberid, routeid, capacityid;
    Button createbutton;
    DatabaseReference reff;
    bus b;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adminaddbus);

        numberid = findViewById(R.id.addbusnumberid);
        routeid = findViewById(R.id.addbusrouteid);
        capacityid = findViewById(R.id.addbuscapacityid);
        createbutton = findViewById(R.id.createbusbutton);

        reff = FirebaseDatabase.getInstance().getReference().child("Bus");
        b = new bus();
        createbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //get values from Edittexts and store them in variables
                String busnumber = numberid.getText().toString();
                String route = routeid.getText().toString();
                String capacity = capacityid.getText().toString();
                if(!busnumber.isEmpty() && !route.isEmpty() && !capacity.isEmpty()) {
                    //set the values for the bus "b" to later on save the bus to the database
                    b.setBus_number(busnumber);
                    b.setBus_route(route);
                    b.setBus_capacity(capacity);
                    b.setOnline(false);
                    b.setP2pshared(false);
                    b.setRequested(false);

                    //creates a new child with attributes = bus b
                    reff.push().setValue(b).addOnCompleteListener(AdminAddBusActivity.this, new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(!task.isSuccessful()){
                                Toast.makeText(AdminAddBusActivity.this, "Process failed, try again", Toast.LENGTH_SHORT).show();
                            }
                            else{
                                Toast.makeText(AdminAddBusActivity.this, "Bus added successfully", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
                else{
                    Toast.makeText(AdminAddBusActivity.this,"Please fill all the fields", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
    @Override
    protected void onStart() {
        super.onStart();
    }

}
