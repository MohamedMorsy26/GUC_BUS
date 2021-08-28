package com.example.herewego;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class DriverChooseBusActivity extends AppCompatActivity {
    FirebaseAuth mFirebaseAuth;
    ListView allBusesListView;
    private DatabaseReference ref = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driverchoosebus);
        allBusesListView = findViewById(R.id.driverchoosebuslistviewid);
        mFirebaseAuth = FirebaseAuth.getInstance();
        final String phonenumber = mFirebaseAuth.getCurrentUser().getEmail().substring(0,11);
        ref = FirebaseDatabase.getInstance().getReference();

        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        ref.child("Bus").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange( DataSnapshot dataSnapshot) {
                final ArrayList<MenuBus> busses = new ArrayList<MenuBus>();
                for(DataSnapshot busSnapShot : dataSnapshot.getChildren()){
                    MenuBus menuBus = new MenuBus((busSnapShot.child("bus_number")).getValue(String.class),
                            busSnapShot.child("bus_route").getValue(String.class), busSnapShot.child("online").getValue(Boolean.class),
                            busSnapShot.child("requested").getValue(Boolean.class), busSnapShot.child("p2pshared").getValue(Boolean.class), false);
                    if(!menuBus.getRoute().equals("Select a bus")){
                        busses.add(menuBus);
                    }
                }
                MenuBusAdapter adapter = new MenuBusAdapter(DriverChooseBusActivity.this, R.layout.all_buses_item, R.id.boibusrouteid, busses);
                allBusesListView.setAdapter(adapter);
                allBusesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(final AdapterView<?> parent, View view, final int position, long id) {
                        final String route = ((MenuBus)parent.getItemAtPosition(position)).getRoute();
                        if(!route.equals("Select a bus")){
                            ref.child("Bus").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot snapshot) {
                                    for(DataSnapshot dataSnapshot: snapshot.getChildren()){
                                        if(dataSnapshot.child("bus_route").getValue(String.class).equals(route)){
                                            drives intentDrives = new drives();
                                            intentDrives.setPhonenumber(phonenumber);
                                            intentDrives.setBusroute(((MenuBus) parent.getItemAtPosition(position)).getRoute());
                                            intentDrives.setBusnumber(((MenuBus) parent.getItemAtPosition(position)).getBusNumber());

                                            Intent inttodrivermap = new Intent(DriverChooseBusActivity.this, DriverMap.class);
                                            inttodrivermap.putExtra("intentDrives", intentDrives);
                                            startActivity(inttodrivermap);
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
            }

            @Override
            public void onCancelled( DatabaseError error) {}
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
            new AlertDialog.Builder(DriverChooseBusActivity.this)
                    .setTitle("Logout?")
                    // Specifying a listener allows you to take an action before dismissing the dialog.
                    // The dialog is automatically dismissed when a dialog button is clicked.
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            mFirebaseAuth = FirebaseAuth.getInstance();
                            final String phonenumber = mFirebaseAuth.getCurrentUser().getEmail().substring(0,11);
                            final String[] route = {""};
                            ref.child("drives").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    for (DataSnapshot drivesSnapshot : snapshot.getChildren()) {
                                        if (drivesSnapshot.child("phonenumber").getValue(String.class).equals(phonenumber)) {
                                            route[0] = drivesSnapshot.child("busroute").getValue(String.class);
                                            ref.child("Bus").addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot busdataSnapshot) {
                                                    //loop over all children of the "Bus" table
                                                    for (DataSnapshot bussnapshot : busdataSnapshot.getChildren()) {
                                                        //check the current datasnapshot if it has the same route as the new bus
                                                        if (bussnapshot.child("bus_route").getValue(String.class).equals(route[0])){
                                                            //if so, make that new bus offline
                                                            bussnapshot.getRef().child("online").setValue(false);
                                                        }
                                                    }
                                                }
                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {}
                                            });
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

                            //also set the route and bus number of the drives entry of the current driver to "nothing""
                            //to signify that he isn't driving anything now

                            mFirebaseAuth.signOut();
                            Intent inttologin = new Intent(DriverChooseBusActivity.this, ChooseTypeActivity.class);
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