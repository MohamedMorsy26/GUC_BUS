package com.example.herewego;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class UserMap extends AppCompatActivity implements
        GoogleMap.OnMyLocationClickListener,
        OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback {
    TextView busnumbertext;
    Button shareButton, onbusbutton, showbusbutton;
    private GoogleMap mMap;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private boolean permissionDenied = false;
    LocationManager locationManager;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private double userlon = 0;
    private double userlat = 0;
    private DatabaseReference ref = null;
    private String email = "";
    private String route = "";
    private String busnumber = "";
    private String phonenumber = "";
    private boolean isSharing = false;
    private String firstName = "";
    private String lastName = "";
    FirebaseAuth mFirebaseAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usermap);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.usermapid);
        mapFragment.getMapAsync(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        busnumbertext = findViewById(R.id.usermapbusnumberid);
        shareButton = findViewById(R.id.usermapstartbutton);
        onbusbutton = findViewById(R.id.usermaponbusbutton);
        showbusbutton = findViewById(R.id.usermapshowbus);
        ref = FirebaseDatabase.getInstance().getReference();
        mFirebaseAuth = FirebaseAuth.getInstance();

        //receive the data in the intent from the UserChooseBusActivity to be used here
        final takes intentTakes = (takes) getIntent().getSerializableExtra("intentTakes");
        busnumbertext.setText(intentTakes.getBusnumber());
        phonenumber = (intentTakes.getPhonenumber());

        //storing the email and busroute in these variables to use them in the map loops
        email = intentTakes.getUser_email();
        route = intentTakes.getBusroute();
        busnumber = intentTakes.getBusnumber();
        getSupportActionBar().setTitle(route);

        //get the user's first name and last name to store them to use later on for the log item
        ref.child("Passengers").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot userSnapshot : snapshot.getChildren()){
                    if(userSnapshot.child("email").getValue(String.class).equals(mFirebaseAuth.getCurrentUser().getEmail())){
                        firstName = userSnapshot.child("firstname").getValue(String.class);
                        lastName = userSnapshot.child("lastname").getValue(String.class);
                        break;
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        //check if the the user is broadcasting their location or not to configure the share button's color
        //and carry that information forward
        ref.child("takes").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot takesSnapshot : snapshot.getChildren()){
                    if(takesSnapshot.child("busroute").getValue(String.class).equals(route)
                            && takesSnapshot.child("user_email").getValue(String.class).equals(email)){
                        isSharing = true;
                        shareButton.setBackgroundResource(R.drawable.button_offline);
                        shareButton.setText("Stop Broadcasting");
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isSharing) {
                    isSharing = true;
                    shareButton.setBackgroundResource(R.drawable.button_offline);
                    shareButton.setText("Stop Broadcasting");
                    ref.child("takes").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            //check if the "takes" table already contains an entry for this user
                            boolean userFound = false;
                            for (DataSnapshot takesSnapshot : dataSnapshot.getChildren()) {
                                if (takesSnapshot.child("user_email").getValue(String.class).equals(intentTakes.getUser_email())) {
                                    userFound = true;
                                    takesSnapshot.getRef().child("busnumber").setValue(intentTakes.getBusnumber());
                                    takesSnapshot.getRef().child("busroute").setValue(intentTakes.getBusroute());
                                    takesSnapshot.getRef().child("phonenumber").setValue(intentTakes.getPhonenumber());
                                    takesSnapshot.getRef().child("user_lat").setValue(userlat);
                                    takesSnapshot.getRef().child("user_lon").setValue(userlon);
                                    //create log item
                                    logItem item = new logItem();
                                    //get the calendar
                                    Calendar calendar = Calendar.getInstance();
                                    //create a custom date format
                                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");
                                    //get the date using that format as a string
                                    String dateTime = simpleDateFormat.format(calendar.getTime());
                                    //get each date component separately (very inefficient)
                                    String day = dateTime.substring(0,2);
                                    String month = dateTime.substring(3,5);
                                    String year = dateTime.substring(6,10);
                                    String hour = dateTime.substring(11,13);
                                    String minute = dateTime.substring(14);
                                    //set the logItem attributes
                                    item.setDay(day);
                                    item.setMonth(month);
                                    item.setYear(year);
                                    item.setHour(hour);
                                    item.setMinute(minute);
                                    //create the text
                                    String text = "["+day+"/"+month+"/"+year+"] At " +hour+":"+minute+", user "+firstName+" "+lastName+
                                            " started broadcasting their location to bus "+busnumber+" taking the "+route+" route.";
                                    item.setText(text);
                                    //push the log to the database
                                    ref.child("Logs").push().setValue(item);

                                }
                            }
                            //if an entry wasn't found, create a new entry
                            //an entry not found means this is the first time a user is ever sharing his location
                            //this is done instead of adding an entry manually in the database
                            if (!userFound) {
                                //add a new "takes" entry with the current driver number, route and bus number
                                final takes newTakes = new takes();
                                newTakes.setBusnumber(intentTakes.getBusnumber());
                                newTakes.setBusroute(intentTakes.getBusroute());
                                newTakes.setPhonenumber(intentTakes.getPhonenumber());
                                newTakes.setUser_email(intentTakes.getUser_email());
                                newTakes.setUser_lat(userlat);
                                newTakes.setUser_lon(userlon);
                                ref.child("takes").push().setValue(newTakes);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {

                        }
                    });
                }
                else if(isSharing){
                    isSharing = false;
                    shareButton.setBackgroundResource(R.drawable.button_online);
                    shareButton.setText("Start broadcasting");
                    //also set the route and bus number of the takes entry of the current user to "nothing"
                    //to signify that they aren't riding anything now
                    ref.child("takes").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot takesSnapshot : snapshot.getChildren()) {
                                if (takesSnapshot.child("user_email").getValue(String.class).equals(intentTakes.getUser_email())) {
                                    takesSnapshot.getRef().child("busnumber").setValue("nothing");
                                    takesSnapshot.getRef().child("busroute").setValue("nothing");
                                    takesSnapshot.getRef().child("phonenumber").setValue("nothing");
                                    //set the location to be (0,0) to be out of the driver's vision
                                    takesSnapshot.getRef().child("user_lat").setValue(0);
                                    takesSnapshot.getRef().child("user_lon").setValue(0);
                                    //create log item
                                    logItem item = new logItem();
                                    //get the calendar
                                    Calendar calendar = Calendar.getInstance();
                                    //create a custom date format
                                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");
                                    //get the date using that format as a string
                                    String dateTime = simpleDateFormat.format(calendar.getTime());
                                    //get each date component separately (very inefficient)
                                    String day = dateTime.substring(0,2);
                                    String month = dateTime.substring(3,5);
                                    String year = dateTime.substring(6,10);
                                    String hour = dateTime.substring(11,13);
                                    String minute = dateTime.substring(14);
                                    //set the logItem attributes
                                    item.setDay(day);
                                    item.setMonth(month);
                                    item.setYear(year);
                                    item.setHour(hour);
                                    item.setMinute(minute);
                                    //create the text
                                    String text = "["+day+"/"+month+"/"+year+"] At " +hour+":"+minute+", user "+firstName+" "+lastName+
                                            " stopped broadcasting their location to bus "+busnumber+" taking the "+route+" route.";
                                    item.setText(text);
                                    //push the log to the database
                                    ref.child("Logs").push().setValue(item);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }
        });

        onbusbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //create log item
                logItem item = new logItem();
                //get the calendar
                Calendar calendar = Calendar.getInstance();
                //create a custom date format
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");
                //get the date using that format as a string
                String dateTime = simpleDateFormat.format(calendar.getTime());
                //get each date component separately (very inefficient)
                String day = dateTime.substring(0,2);
                String month = dateTime.substring(3,5);
                String year = dateTime.substring(6,10);
                String hour = dateTime.substring(11,13);
                String minute = dateTime.substring(14);
                //set the logItem attributes
                item.setDay(day);
                item.setMonth(month);
                item.setYear(year);
                item.setHour(hour);
                item.setMinute(minute);
                //create the text
                String text = "["+day+"/"+month+"/"+year+"] At " +hour+":"+minute+", user "+firstName+" "+lastName+
                        " got on bus "+busnumber+" taking the "+route+" route.";
                item.setText(text);
                //push the log to the database
                ref.child("Logs").push().setValue(item);
            }
        });

        showbusbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //get the bus with the current selected route
                ref.child("drives").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        //loop over all online busses
                        for(DataSnapshot bussnapshot: snapshot.getChildren()){
                            //get the bus with the current selected route
                            if(bussnapshot.child("busroute").getValue(String.class).equals(route)){
                                double curlon = bussnapshot.child("buslon").getValue(Double.class);
                                double curlat = bussnapshot.child("buslat").getValue(Double.class);
                                LatLng buslocation = new LatLng(curlat, curlon);
                                //move the camera to that bus's location
                                mMap.moveCamera(CameraUpdateFactory.newLatLng(buslocation));
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */


    @Override
    public void onMapReady(GoogleMap googleMap) {
        final boolean[] doOnce = {false};
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //return;
        }

        //Location myLocation;
        final double[] myloclong = new double[1];
        final double[] myloclat = new double[1];
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                // Got last known location. In some rare situations this can be null.
                if (location != null) {
                    // Logic to handle location object
                    myloclat[0] = location.getLatitude();
                    myloclong[0] = location.getLongitude();
                }
            }
        });

        mMap = googleMap;
        mMap.setOnMyLocationClickListener(this);
        enableMyLocation();
        LocationRequest lr = new LocationRequest();
        lr.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        lr.setInterval(2000);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    myloclat[0] = location.getLatitude();
                    myloclong[0] = location.getLongitude();
                    LatLng mylocation = new LatLng(myloclat[0], myloclong[0]);
                    if(!doOnce[0]){
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(mylocation));
                        doOnce[0] = true;
                    }
                    //storing the lat and lon of the user in the takes table for the driver to see them
                    userlon = location.getLongitude();
                    userlat = location.getLatitude();

                    //upload that location to the takes table in the database continuously
                    ref.child("takes").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for(DataSnapshot takesSnapshot : snapshot.getChildren()){
                                //find the entry of the current user using his email
                                if(takesSnapshot.child("user_email").getValue(String.class).equals(email)){
                                    //check if he is sharing his current location
                                    if(!takesSnapshot.child("busroute").getValue(String.class).equals("nothing")){
                                        //update the location in the takes entry
                                        takesSnapshot.getRef().child("user_lat").setValue(userlat);
                                        takesSnapshot.getRef().child("user_lon").setValue(userlon);
                                    }

                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                        }
                    });

                    //clear previous markers
                    mMap.clear();
                    ref.child("drives").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for(DataSnapshot drivesSnapshot : snapshot.getChildren()){
                                String drivesroute = drivesSnapshot.child("busroute").getValue(String.class);
                                //add marker on the map at the bus's location
                                if(drivesroute.equals(route)){
                                    double buslat = drivesSnapshot.child("buslat").getValue(double.class);
                                    double buslon = drivesSnapshot.child("buslon").getValue(double.class);
                                    LatLng buslocation = new LatLng(buslat, buslon);
                                    mMap.addMarker(new MarkerOptions().position(buslocation));
                                }
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                        }
                    });

                }
            }
        };
        startLocationUpdates(lr, locationCallback);

        mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        mMap.setMinZoomPreference(15);
        mMap.setMaxZoomPreference(18);
    }

    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */
    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            if (mMap != null) {
                mMap.setMyLocationEnabled(true);
            }
        } else {
            // Permission to access the location is missing. Show rationale and request permission
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        }

    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        Toast.makeText(this, "Current location:\n" + location, Toast.LENGTH_LONG).show();
    }

    //Permission functions
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults, Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation();
        } else {
            // Permission was denied. Display an error message
            // Display the missing permission error dialog when the fragments resume.
            permissionDenied = true;
        }
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (permissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError();
            permissionDenied = false;
        }
    }

    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     */
    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getSupportFragmentManager(), "dialog");
    }

    private void startLocationUpdates(LocationRequest locationRequest, LocationCallback locationCallback) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.requestLocationUpdates(locationRequest,
                locationCallback,
                Looper.getMainLooper());
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.user_map_action_bar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.action_call_driver){
            //call the driver's phonenumber passed from the intent using an Intent.ACTION_DIAL
            Intent callDriver = new Intent(Intent.ACTION_DIAL);
            callDriver.setData(Uri.parse("tel:" + phonenumber));
            startActivity(callDriver);
        }
        else if(item.getItemId() == android.R.id.home){
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


}