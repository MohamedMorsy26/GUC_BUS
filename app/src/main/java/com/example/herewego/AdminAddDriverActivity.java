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
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AdminAddDriverActivity extends AppCompatActivity {
    EditText phonenumberid, firstnameid, lastnameid, macaddressid;
    Button createbutton;
    DatabaseReference reff;
    driver d;
    FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adminadddriver);

        mFirebaseAuth = FirebaseAuth.getInstance();
        phonenumberid = findViewById(R.id.adddriverphonenumber);
        firstnameid = findViewById(R.id.adddriverfirstnameid);
        lastnameid = findViewById(R.id.adddriverlastnameid);
        macaddressid = findViewById(R.id.adddrivermacid);
        createbutton = findViewById(R.id.createdriverbutton);
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {}};

        reff = FirebaseDatabase.getInstance().getReference().child("Drivers");
        d = new driver();
        createbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //get values from Edittexts and store them in variables
                String phonenumber = phonenumberid.getText().toString();
                String fname = firstnameid.getText().toString();
                String lname = lastnameid.getText().toString();
                String macadd = macaddressid.getText().toString();
                String email = phonenumber + "@driver.guc.edu.eg";
                if(!phonenumber.isEmpty() && !fname.isEmpty() && !lname.isEmpty() && !macadd.isEmpty()) {
                    //set the values for the driver "d" to later on save the driver to the database
                    d.setPhonenumber(phonenumber);
                    d.setFirstname(fname);
                    d.setLastname(lname);
                    d.setMacaddress(macadd);

                    mFirebaseAuth.createUserWithEmailAndPassword(email, "password").
                            addOnCompleteListener(AdminAddDriverActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if(!task.isSuccessful()){
                                        Toast.makeText(AdminAddDriverActivity.this, "Process failed, try again", Toast.LENGTH_SHORT).show();
                                    }
                                    else{
                                        Toast.makeText(AdminAddDriverActivity.this, "Driver added successfully", Toast.LENGTH_SHORT).show();
                                        //creates a new child with attributes = driver d
                                        reff.push().setValue(d);
                                    }
                                }
                            });
                }
                else{
                    Toast.makeText(AdminAddDriverActivity.this,"Please fill all the fields", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
    @Override
    protected void onStart() {
        super.onStart();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

}
