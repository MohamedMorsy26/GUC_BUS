package com.example.herewego;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

public class UserLoginActivity extends AppCompatActivity {
    EditText emailId;
    Button btnSignIn;
    FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private String mac = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userlogin);

        mFirebaseAuth = FirebaseAuth.getInstance();
        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        emailId = findViewById(R.id.emailtext);
        btnSignIn = findViewById(R.id.loginbutton);

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {}
        };

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailId.getText().toString();
                email = email + "@student.guc.edu.eg";
                final String pass = "password";
                final String finalEmail = email;
                final String deviceMACAddress = getMacAddr();
                final ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

                mFirebaseAuth.signInWithEmailAndPassword(finalEmail, pass).addOnCompleteListener(UserLoginActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(!task.isSuccessful()){
                                    //login can fail due to no internet access :'D
                                    if(!(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED) &&
                                            !(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED)){
                                        Toast.makeText(UserLoginActivity.this, "Please check your internet connection then try again", Toast.LENGTH_SHORT).show();
                                    }
                                    else {
                                        Toast.makeText(UserLoginActivity.this, "Wrong username", Toast.LENGTH_SHORT).show();
                                    }
                                }
                                else{
                                    final boolean[] isEnabled = {true};
                                    ref.child("Passengers").addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            for(DataSnapshot passengerSnapshot: snapshot.getChildren()){
                                                if(passengerSnapshot.child("email").getValue(String.class).equals(finalEmail)){
                                                    isEnabled[0] = passengerSnapshot.child("enabled").getValue(Boolean.class);
                                                    mac = passengerSnapshot.child("macaddress").getValue(String.class);
                                                }
                                            }
                                        }
                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {}
                                    });
                                    //TODO comment this MAC part to test using an emulator as emulators don't have MAC addresses
                                    /*new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                           //compare the device's mac address with the user's mac address saved in the database
                                            if(!mac.equals(deviceMACAddress)){
                                                //Different macs mean that someone is trying to sign in using another user's info
                                                //So, display a toast telling them they got caught :D
                                                Log.e("mac in database is", mac);
                                                Log.e("mac of device is", deviceMACAddress);
                                                Toast.makeText(UserLoginActivity.this, "Username and device don't match", Toast.LENGTH_SHORT).show();
                                            }
                                            else {
                                                if (isEnabled[0]) {
                                                   Intent intToHome = new Intent(UserLoginActivity.this, UserMenuActivity.class);
                                                    //intToHome.putExtra("user", intentUser);
                                                    startActivity(intToHome);
                                                } else {
                                                    Toast.makeText(UserLoginActivity.this, "User isn't allowed to use the bus", Toast.LENGTH_LONG).show();
                                                }
                                            }
                                        }
                                    },1000);*/
                                    //TODO comment this part if not gonna test using emulator
                                    if (isEnabled[0]) {
                                        Intent intToHome = new Intent(UserLoginActivity.this, UserMenuActivity.class);
                                        //intToHome.putExtra("user", intentUser);
                                        startActivity(intToHome);
                                    } else {
                                        Toast.makeText(UserLoginActivity.this, "User isn't allowed to use the bus", Toast.LENGTH_LONG).show();
                                    }
                                }
                            }
                        });


            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    //this method is used to obtain the MAC Address of the user's device.
    //unfortunately, I have no idea how it works but, it works :D
    public static String getMacAddr() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;

                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return "";
                }

                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    String hex = Integer.toHexString(b & 0xFF);
                    if (hex.length() == 1)
                        hex = "0".concat(hex);
                    res1.append(hex.concat(":"));
                }

                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString();
            }
        } catch (Exception ex) {
        }
        return "";
    }
}