package com.example.herewego;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
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


public class DriverMap extends AppCompatActivity implements
        GoogleMap.OnMyLocationClickListener,
        OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback {
    TextView busnumbertext;
    Button shareButton;
    private GoogleMap mMap;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private boolean permissionDenied = false;
    LocationManager locationManager;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private DatabaseReference ref = null;
    private String route = "";
    private String phonenumber = "";
    private final double[] myloclong = new double[1];
    private final double[] myloclat = new double[1];
    FirebaseAuth mFirebaseAuth;
    private boolean isSharing = false;
    private String firstName = "";
    private String lastName = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drivermap);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mFirebaseAuth = FirebaseAuth.getInstance();
        shareButton = findViewById(R.id.drivermapsharebutton);
        busnumbertext = findViewById(R.id.drivermapbusnumberid);
        ref = FirebaseDatabase.getInstance().getReference();

        //receive the data in the intent from the DriverChooseBusActivity to be used here
        final drives intentDrives = (drives) getIntent().getSerializableExtra("intentDrives");
        busnumbertext.setText(intentDrives.getBusnumber());
        //storing the busroute and driverphonenumber in these variables to use them in the map loops
        route = intentDrives.getBusroute();
        phonenumber = mFirebaseAuth.getCurrentUser().getEmail().substring(0,11);
        getSupportActionBar().setTitle(route);

        //get the driver's first name and last name to store them to use later on for the log item
        ref.child("Drivers").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot driverSnapshot : snapshot.getChildren()){
                    if(driverSnapshot.child("phonenumber").getValue(String.class).equals(phonenumber)){
                        firstName = driverSnapshot.child("firstname").getValue(String.class);
                        lastName = driverSnapshot.child("lastname").getValue(String.class);
                        break;
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        //check if the bus is online or not to configure the share button's color
        //and carry that information forward
        ref.child("drives").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot drivesSnapshot : snapshot.getChildren()){
                    if(drivesSnapshot.child("busroute").getValue(String.class).equals(route)
                    && drivesSnapshot.child("phonenumber").getValue(String.class).equals(phonenumber)){
                        isSharing = true;
                        shareButton.setBackgroundResource(R.drawable.button_offline);
                        shareButton.setText("Stop Sharing");
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });


        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isSharing){
                    isSharing = true;
                    shareButton.setBackgroundResource(R.drawable.button_offline);
                    shareButton.setText("Stop Sharing");
                    ref.child("drives").addListenerForSingleValueEvent(new ValueEventListener() {
                    //need to check that the current driver didn't set any other bus to online as well
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        boolean matchingDriverFound = false;
                        //loop over all entries in the "drives" child
                        for (DataSnapshot drivesSnapshot : dataSnapshot.getChildren()) {
                            //get the number of the driver of this current datasnapshot
                            String driverPhoneNumber = drivesSnapshot.child("phonenumber").getValue(String.class);
                            //check if that number is the same as the number of the current driver
                            //if that's the case, make the old bus offline and the current bus online
                            if (driverPhoneNumber.equals(intentDrives.getPhonenumber())) {
                                matchingDriverFound = true;
                                //get the route of the old bus from the old "drives" entry
                                final String oldbusroute = drivesSnapshot.child("busroute").getValue(String.class);
                                ref.child("Bus").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot busdataSnapshot) {
                                        //loop over all children of the "Bus" table
                                        for (DataSnapshot bussnapshot : busdataSnapshot.getChildren()) {
                                            //check the current datasnapshot if it has the same route as the old bus
                                            if (bussnapshot.child("bus_route").getValue(String.class).equals(oldbusroute)) {
                                                //if so, make that old bus offline
                                                bussnapshot.getRef().child("online").setValue(false);
                                            }
                                        }
                                    }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                                    //set the value of the child bus_route to be the new route
                                    drivesSnapshot.getRef().child("busroute").setValue(intentDrives.getBusroute());
                                    //Tset the value of the child bus_number to be the new number
                                    drivesSnapshot.getRef().child("busnumber").setValue(intentDrives.getBusnumber());

                                }

                            }
                            if (!matchingDriverFound) {
                                //add a new "drives" entry with the current driver number, route and bus number
                                final drives newDrives = new drives();
                                newDrives.setBusnumber(intentDrives.getBusnumber());
                                newDrives.setBusroute(intentDrives.getBusroute());
                                newDrives.setPhonenumber(intentDrives.getPhonenumber());
                                ref.child("drives").push().setValue(newDrives);


                            }
                            //get the bus with the new route and set it to online
                            ref.child("Bus").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot busdataSnapshot) {
                                    //loop over all children of the "Bus" table
                                    for (DataSnapshot bussnapshot : busdataSnapshot.getChildren()) {
                                        //check the current datasnapshot if it has the same route as the new bus
                                        if (bussnapshot.child("bus_route").getValue(String.class).equals(intentDrives.getBusroute())) {
                                            //if so, make that new bus online
                                            bussnapshot.getRef().child("online").setValue(true);
                                            bussnapshot.getRef().child("p2pshared").setValue(false);
                                            bussnapshot.getRef().child("requested").setValue(false);
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
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
                        String text = "["+day+"/"+month+"/"+year+"] At " +hour+":"+minute+", driver "+firstName+" "+lastName+
                                " started broadcasting the location of bus number "+intentDrives.getBusnumber()+" taking the "+route+" route.";
                        item.setText(text);
                        //push the log to the database
                        ref.child("Logs").push().setValue(item);
                        }


                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
                else if(isSharing){
                    isSharing = false;
                    shareButton.setBackgroundResource(R.drawable.button_online);
                    shareButton.setText("Start Sharing");
                    ref.child("Bus").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot busdataSnapshot) {
                            //loop over all children of the "Bus" table
                            for (DataSnapshot bussnapshot : busdataSnapshot.getChildren()) {
                                //check the current datasnapshot if it has the same route as the new bus
                                if (bussnapshot.child("bus_route").getValue(String.class).equals(intentDrives.getBusroute())) {
                                    //if so, make that new bus offline
                                    bussnapshot.getRef().child("online").setValue(false);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                    //also set the route and bus number of the drives entry of the current driver to "nothing""
                    //to signify that he isn't driving anything now
                    ref.child("drives").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot drivesSnapshot : snapshot.getChildren()) {
                                if (drivesSnapshot.child("phonenumber").getValue(String.class).equals(intentDrives.getPhonenumber())) {
                                    drivesSnapshot.getRef().child("busnumber").setValue("nothing");
                                    drivesSnapshot.getRef().child("busroute").setValue("nothing");
                                    drivesSnapshot.getRef().child("buslat").setValue(0);
                                    drivesSnapshot.getRef().child("buslon").setValue(0);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
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
                    String text = "["+day+"/"+month+"/"+year+"] At " +hour+":"+minute+", driver "+firstName+" "+lastName+
                            " started broadcasting the location of bus number "+intentDrives.getBusnumber()+" taking the "+route+" route.";
                    item.setText(text);
                    //push the log to the database
                    ref.child("Logs").push().setValue(item);
                }
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
                for (final Location location : locationResult.getLocations()) {
                    myloclat[0] = location.getLatitude();
                    myloclong[0] = location.getLongitude();
                    LatLng mylocation = new LatLng(myloclat[0], myloclong[0]);
                    if(!doOnce[0]){
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(mylocation));
                        doOnce[0] = true;
                    }
                    //clear previous markers
                    mMap.clear();
                    //add a custom bus marker over the driver's location
                    mMap.addMarker(new MarkerOptions().position(mylocation).icon(BitmapDescriptorFactory.fromResource(R.mipmap.bus_icon_bitmap)));
                    //loop over all "takes" entries, get the ones with the current bus route
                    ref.child("takes").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for(DataSnapshot takesSnapshot : snapshot.getChildren()){
                                String takesroute = takesSnapshot.child("busroute").getValue(String.class);
                                //add markers on the map at their locations
                                if(takesroute.equals(route)){
                                    double takeslat = takesSnapshot.child("user_lat").getValue(double.class);
                                    double takeslon = takesSnapshot.child("user_lon").getValue(double.class);
                                    LatLng takeslocation = new LatLng(takeslat, takeslon);
                                    mMap.addMarker(new MarkerOptions().position(takeslocation));
                                }
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                        }
                    });
                    //update the bus's location in the drives table
                    ref.child("drives").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for(DataSnapshot drivesSnapshot : snapshot.getChildren()){
                                //find the entry of the current bus driver using his phone number
                                if(drivesSnapshot.child("phonenumber").getValue(String.class).equals(phonenumber)){
                                    //check if he is sharing the bus's current location
                                    if(!drivesSnapshot.child("busroute").getValue(String.class).equals("nothing")){
                                        //update the location in the drives entry
                                        drivesSnapshot.getRef().child("buslat").setValue(location.getLatitude());
                                        drivesSnapshot.getRef().child("buslon").setValue(location.getLongitude());
                                    }

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
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
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
}