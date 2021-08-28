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

public class DriverLoginActivity extends AppCompatActivity {
    EditText phonenumberid;
    Button btnSignIn;
    FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private String mac = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driverlogin);

        mFirebaseAuth = FirebaseAuth.getInstance();
        phonenumberid = findViewById(R.id.phonenumberid);
        btnSignIn = findViewById(R.id.driverloginbutton);
        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference();

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
            }
        };

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String phonenumber = phonenumberid.getText().toString();
                final String phonenumberemail = phonenumber + "@driver.guc.edu.eg";
                String pass = "password";
                final String deviceMACAddress = getMacAddr();
                final ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

                if(!phonenumber.isEmpty()){
                    mFirebaseAuth.signInWithEmailAndPassword(phonenumberemail, pass).addOnCompleteListener(DriverLoginActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(!task.isSuccessful()){
                                if(!(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED) &&
                                        !(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED)){
                                    Toast.makeText(DriverLoginActivity.this, "Please check your internet connection then try again", Toast.LENGTH_SHORT).show();
                                }
                                else {
                                    Toast.makeText(DriverLoginActivity.this, "Wrong phone number", Toast.LENGTH_SHORT).show();
                                }
                            }
                            else{
                                ref.child("Drivers").addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        for(DataSnapshot driverSnapshot: snapshot.getChildren()){
                                            if(driverSnapshot.child("phonenumber").getValue(String.class).equals(phonenumber)){
                                                mac = driverSnapshot.child("macaddress").getValue(String.class);
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
                                            Toast.makeText(DriverLoginActivity.this, "Phone number and device don't match", Toast.LENGTH_SHORT).show();
                                        }
                                        else {
                                                Intent intToHome = new Intent(DriverLoginActivity.this, DriverChooseBusActivity.class);
                                                //intToHome.putExtra("user", intentUser);
                                                startActivity(intToHome);
                                        }
                                    }
                                },1000);*/
                                //TODO comment this part if MAC is enabled
                                Intent intToDriverHome = new Intent(DriverLoginActivity.this, DriverChooseBusActivity.class);
                                startActivity(intToDriverHome);
                            }

                        }
                    });
                }
                else{
                    Toast.makeText(DriverLoginActivity.this, "Please fill the empty field", Toast.LENGTH_SHORT).show();
                }
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