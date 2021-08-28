package com.example.herewego;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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

public class AdminLoginActivity extends AppCompatActivity {
    EditText emailId, passwordId;
    Button btnSignIn;
    FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adminlogin);

        mFirebaseAuth = FirebaseAuth.getInstance();
        emailId = findViewById(R.id.adminemailid);
        passwordId = findViewById(R.id.adminpassid);
        btnSignIn = findViewById(R.id.adminloginbutton);

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                /*FirebaseUser mFirebaseUser = mFirebaseAuth.getCurrentUser();
                if(mFirebaseUser != null){
                    Toast.makeText(UserLoginActivity.this, "You are logged in", Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(UserLoginActivity.this, UserChooseBusActivity.class);
                    startActivity(i);
                }
                else{
                    Toast.makeText(UserLoginActivity.this, "Please login", Toast.LENGTH_SHORT).show();
                }*/
            }
        };

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailId.getText().toString(); //admin@admin.com
                final String pass = passwordId.getText().toString(); //admin123
                final ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                if(!email.isEmpty() && !pass.isEmpty()){
                    email = email + "@admin.com";
                    final String finalEmail = email;
                    mFirebaseAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(AdminLoginActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(!task.isSuccessful()){
                                if(!(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED) &&
                                        !(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED)){
                                    Toast.makeText(AdminLoginActivity.this, "Please check your internet connection then try again", Toast.LENGTH_SHORT).show();
                                }
                                else if(!finalEmail.equals("admin@admin.com")){
                                    Toast.makeText(AdminLoginActivity.this, "Wrong username", Toast.LENGTH_SHORT).show();
                                }
                                else if(pass.equals("admin123")){
                                    Toast.makeText(AdminLoginActivity.this, "Wrong password", Toast.LENGTH_SHORT).show();
                                }
                            }
                            else{
                                Intent intToHome = new Intent(AdminLoginActivity.this, AdminHomeActivity.class);
                                startActivity(intToHome);
                            }

                        }
                    });
                }
                else{
                    Toast.makeText(AdminLoginActivity.this, "Please fill the empty field", Toast.LENGTH_SHORT).show();
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