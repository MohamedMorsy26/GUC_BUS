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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Userp2phostmap extends AppCompatActivity implements
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
    private final double[] myloclong = new double[1];
    private final double[] myloclat = new double[1];
    FirebaseAuth mFirebaseAuth;
    private boolean isSharing = false;
    private boolean isOnline = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userp2phostmap);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.userp2phostmapid);
        mapFragment.getMapAsync(this);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mFirebaseAuth = FirebaseAuth.getInstance();
        shareButton = findViewById(R.id.userp2phostmapsharebutton);
        busnumbertext = findViewById(R.id.userp2phostmapbusnumberid);
        ref = FirebaseDatabase.getInstance().getReference();

        //receive the data in the intent from the UserMenuActivity to be used here
        final p2pshare intentp2pshare = (p2pshare) getIntent().getSerializableExtra("intentp2pshare");
        busnumbertext.setText(intentp2pshare.getBusnumber());
        //storing the busroute in this variable to use it in the map loops
        route = intentp2pshare.getBusroute();
        getSupportActionBar().setTitle(route);

        //check if the bus is online or not to configure the share button's color
        //and carry that information forward
        ref.child("Bus").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot busSnapshot : snapshot.getChildren()){
                    if(busSnapshot.child("bus_route").getValue(String.class).equals(route)){
                        if(busSnapshot.child("online").getValue(boolean.class)){
                            isOnline = true;
                        }
                        else if(!busSnapshot.child("online").getValue(boolean.class)){
                            isOnline = false;
                            if(busSnapshot.child("p2pshared").getValue(boolean.class)){
                                isSharing = true;
                                shareButton.setBackgroundResource(R.drawable.button_offline);
                                shareButton.setText("Stop Sharing");
                            }
                            else{
                                isSharing = false;
                                shareButton.setBackgroundResource(R.drawable.button_online);
                                shareButton.setText("Start Sharing");
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isOnline) {
                    if (!isSharing) {
                        isSharing = true;
                        shareButton.setBackgroundResource(R.drawable.button_offline);
                        shareButton.setText("Stop Sharing");
                        ref.child("p2pshare").addListenerForSingleValueEvent(new ValueEventListener() {
                            //need to check that the current p2p host didn't set any other bus to p2pshared as well
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                boolean matchingHostFound = false;
                                //loop over all entries in the "p2pshare" child
                                for (DataSnapshot p2pSnapshot : dataSnapshot.getChildren()) {
                                    //get the email of the host of this current datasnapshot
                                    String hostEmail = p2pSnapshot.child("email").getValue(String.class);
                                    //check if that email is the same as the email of the current host
                                    //if that's the case, make the old bus !p2pshared and the current bus p2pshared
                                    if (hostEmail.equals(intentp2pshare.getEmail())){
                                        matchingHostFound = true;
                                        //get the route of the old bus from the old "p2pshare" entry
                                        final String oldbusroute = p2pSnapshot.child("busroute").getValue(String.class);
                                        ref.child("Bus").addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot busdataSnapshot) {
                                                //loop over all children of the "Bus" table
                                                for (DataSnapshot bussnapshot : busdataSnapshot.getChildren()) {
                                                    //check the current datasnapshot if it has the same route as the old bus
                                                    if (bussnapshot.child("bus_route").getValue(String.class).equals(oldbusroute)) {
                                                        //if so, make that old bus !p2pshared
                                                        bussnapshot.getRef().child("p2pshared").setValue(false);
                                                        bussnapshot.getRef().child("requested").setValue(false);
                                                    }
                                                }
                                            }
                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });
                                        //set the value of the child bus_route to be the new route
                                        p2pSnapshot.getRef().child("busroute").setValue(intentp2pshare.getBusroute());
                                        //set the value of the child bus_number to be the new number
                                        p2pSnapshot.getRef().child("busnumber").setValue(intentp2pshare.getBusnumber());
                                    }
                                }
                                if (!matchingHostFound) {
                                    //add a new "p2pshare" entry with the current host email, route and bus number
                                    final p2pshare newP2pshare = new p2pshare();
                                    newP2pshare.setBusnumber(intentp2pshare.getBusnumber());
                                    newP2pshare.setBusroute(intentp2pshare.getBusroute());
                                    newP2pshare.setEmail(intentp2pshare.getEmail());
                                    ref.child("p2pshare").push().setValue(newP2pshare);
                                }
                                //get the bus with the new route and make it p2pshared
                                ref.child("Bus").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot busdataSnapshot) {
                                        //loop over all children of the "Bus" table
                                        for (DataSnapshot bussnapshot : busdataSnapshot.getChildren()) {
                                            //check the current datasnapshot if it has the same route as the new bus
                                            if (bussnapshot.child("bus_route").getValue(String.class).equals(intentp2pshare.getBusroute())) {
                                                //if so, make that new bus p2pshared
                                                bussnapshot.getRef().child("p2pshared").setValue(true);
                                                bussnapshot.getRef().child("requested").setValue(false);
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    } else if (isSharing) {
                        isSharing = false;
                        shareButton.setBackgroundResource(R.drawable.button_online);
                        shareButton.setText("Start Sharing");
                        ref.child("Bus").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot busdataSnapshot) {
                                //loop over all children of the "Bus" table
                                for (DataSnapshot bussnapshot : busdataSnapshot.getChildren()) {
                                    //check the current datasnapshot if it has the same route as the new bus
                                    if (bussnapshot.child("bus_route").getValue(String.class).equals(intentp2pshare.getBusroute())) {
                                        //if so, make that new bus !p2pshared
                                        bussnapshot.getRef().child("p2pshared").setValue(false);
                                        bussnapshot.getRef().child("requested").setValue(false);
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                        //also set the route and bus number of the p2pshare entry of the current host to "nothing"
                        //to signify that they aren't sharing anything now
                        ref.child("p2pshare").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot p2pSnapshot : snapshot.getChildren()) {
                                    if (p2pSnapshot.child("email").getValue(String.class).equals(intentp2pshare.getEmail())){
                                        p2pSnapshot.getRef().child("busnumber").setValue("nothing");
                                        p2pSnapshot.getRef().child("busroute").setValue("nothing");
                                        p2pSnapshot.getRef().child("buslat").setValue(0);
                                        p2pSnapshot.getRef().child("buslon").setValue(0);
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
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

                    //update the bus's location in the p2pshare table
                    ref.child("p2pshare").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for(DataSnapshot p2pSnapshot : snapshot.getChildren()){
                                //find the entry of the current host using their email
                                if(mFirebaseAuth.getCurrentUser()!=null && p2pSnapshot.child("email").getValue(String.class).equals(
                                        mFirebaseAuth.getCurrentUser().getEmail())){
                                    //check if they are sharing the bus's current location
                                    if(!p2pSnapshot.child("busroute").getValue(String.class).equals("nothing")){
                                        //update the location in the p2pshare entry
                                        p2pSnapshot.getRef().child("buslat").setValue(location.getLatitude());
                                        p2pSnapshot.getRef().child("buslon").setValue(location.getLongitude());
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