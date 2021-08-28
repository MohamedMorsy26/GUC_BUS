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

import java.util.ArrayList;

public class AdminAddUserActivity extends AppCompatActivity {
    EditText emailid, firstnameid, lastnameid, macaddressid;
    Button createbutton;
    DatabaseReference reff;
    user u;
    FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adminadduser);

        mFirebaseAuth = FirebaseAuth.getInstance();
        emailid = findViewById(R.id.adduseremailid);
        firstnameid = findViewById(R.id.adduserfirstnameid);
        lastnameid = findViewById(R.id.adduserlastnameid);
        macaddressid = findViewById(R.id.addusermacid);
        createbutton = findViewById(R.id.createuserbutton);
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {}};

        reff = FirebaseDatabase.getInstance().getReference().child("Passengers");
        u = new user();
        createbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //get values from Edittexts and store them in variables
                String email = emailid.getText().toString();
                String fname = firstnameid.getText().toString();
                String lname = lastnameid.getText().toString();
                String macadd = macaddressid.getText().toString();
                if(!email.isEmpty() && !fname.isEmpty() && !lname.isEmpty() && !macadd.isEmpty()) {
                    //set the values for the user "u" to later on save the user to the database
                    u.setEmail(email);
                    u.setFirstname(fname);
                    u.setLastname(lname);
                    u.setMacaddress(macadd);
                    u.setEnabled(true);
                    u.setUserlat(0);
                    u.setUserlon(0);
                    ArrayList<String> arr = new ArrayList<>();
                    arr.add("Default");
                    u.setBois(arr);

                    mFirebaseAuth.createUserWithEmailAndPassword(email, "password").
                            addOnCompleteListener(AdminAddUserActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(!task.isSuccessful()){
                                Toast.makeText(AdminAddUserActivity.this, "Process failed, try again", Toast.LENGTH_SHORT).show();
                            }
                            else{
                                Toast.makeText(AdminAddUserActivity.this, "User added successfully", Toast.LENGTH_SHORT).show();
                                //creates a new child with attributes = user u
                                reff.push().setValue(u);
                            }
                        }
                    });
                }
                else{
                    Toast.makeText(AdminAddUserActivity.this,"Please fill all the fields", Toast.LENGTH_LONG).show();
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
