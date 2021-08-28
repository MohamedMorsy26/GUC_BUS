package com.example.herewego;

import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

public class UserMenuActivity extends AppCompatActivity {
    //boi = buses of interest
    FirebaseAuth mFirebaseAuth;
    TextView circleOnline, circleOffline, circleRequested;
    ListView favourites;
    private String route = "";
    private final String ONLINE_CHANNEL_ID = "online_notifications";
    private final String SHARE_CHANNEL_ID = "share_notifications";

    ArrayList<MenuBus> allbuses = new ArrayList<MenuBus>();
    private BOIAdapter boiAdapter;
    final DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
    private boolean isRequested = false;
    private boolean isShared = false;
    private boolean isOnline = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usermenu);
        createOnlineNotificationChannel();
        createShareNotificationChannel();

        favourites = findViewById(R.id.usermenufavouriteslistviewid);
        mFirebaseAuth = FirebaseAuth.getInstance();

        //Getting the Legend circles and coloring them up
        circleOffline = findViewById(R.id.circleofflineid);
        circleOnline = findViewById(R.id.circleonlineid);
        circleRequested = findViewById(R.id.circlerequestedid);
        GradientDrawable circleOfflineBackground = (GradientDrawable) circleOffline.getBackground();
        GradientDrawable circleOnlineBackground = (GradientDrawable) circleOnline.getBackground();
        GradientDrawable circleRequestedBackground = (GradientDrawable) circleRequested.getBackground();
        circleOfflineBackground.setColor(getResources().getColor(R.color.statusoffline));
        circleOnlineBackground.setColor(getResources().getColor(R.color.statusonline));
        circleRequestedBackground.setColor(getResources().getColor(R.color.requested));

        //filling the "userBoiList"
        ref.child("Bus").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //Filling the "allbuses" arraylist with all the buses in the database in a "menubus" format
                ref.child("Bus").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        allbuses.clear();
                        for(DataSnapshot busSnapshot : snapshot.getChildren()){
                            MenuBus menuBus = new MenuBus(
                                    busSnapshot.child("bus_number").getValue(String.class),
                                    busSnapshot.child("bus_route").getValue(String.class),
                                    busSnapshot.child("online").getValue(boolean.class),
                                    busSnapshot.child("requested").getValue(boolean.class),
                                    busSnapshot.child("p2pshared").getValue(boolean.class),
                                    false
                            );
                            ref.child("Passengers").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    for(DataSnapshot userSnapshot : snapshot.getChildren()){
                                        if(mFirebaseAuth.getCurrentUser()!=null && userSnapshot.child("email").getValue(String.class).equals(mFirebaseAuth.getCurrentUser().getEmail())){
                                            for(DataSnapshot arraySnapshot : userSnapshot.child("bois").getChildren()){
                                                for(MenuBus bus : allbuses){
                                                    if(bus.getRoute().equals(arraySnapshot.getValue(String.class))){
                                                        bus.setFavourite(true);
                                                        boiAdapter.notifyDataSetChanged();
                                                    }
                                                }
                                            }
                                            Collections.sort(allbuses);
                                            boiAdapter.notifyDataSetChanged();
                                        }
                                    }
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {}
                            });
                            if(!menuBus.getRoute().equals("Select a bus")){
                                allbuses.add(menuBus);
                            }
                        }
                        boiAdapter.notifyDataSetChanged();
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        //function for clicking the star button: change the isFavourite of the bus, update the user's boi list and refresh the adapter
        boiAdapter = new BOIAdapter(UserMenuActivity.this, allbuses, new BOIAdapter.customButtonClickListener() {
            @Override
            public void onStarButtonClickListener(int position) {
                final MenuBus selectedBoi = allbuses.get(position);
                //loop over all buses, find the bus and change its isFavourite

                ref.child("Passengers").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot usersnapshot : snapshot.getChildren()){
                            if(mFirebaseAuth.getCurrentUser()!=null && usersnapshot.child("email").getValue(String.class).equals(mFirebaseAuth.getCurrentUser().getEmail())){
                                //if it was a favourite, remove it then push the new list
                                if(selectedBoi.isFavourite()){
                                    //create a new list with the remaining favourite buses
                                    ArrayList<String> newUserList = new ArrayList<>();
                                    for(MenuBus bus : allbuses){
                                        if(!bus.getRoute().equals(selectedBoi.getRoute()) && bus.isFavourite()){
                                            newUserList.add(bus.getRoute());
                                        }
                                    }
                                    newUserList.add("Default");
                                    usersnapshot.getRef().child("bois").setValue(newUserList);
                                    //make the bus no longer a favourite
                                    for(MenuBus menuBus : allbuses){
                                        if(menuBus.getRoute().equals(selectedBoi.getRoute())){
                                            menuBus.setFavourite(false);
                                            Collections.sort(allbuses);
                                            boiAdapter.notifyDataSetChanged();
                                            break;
                                        }
                                    }
                                    boiAdapter.notifyDataSetChanged();
                                }
                                //if it was not a favourite, add it then push the new list
                                else if(!selectedBoi.isFavourite()){
                                    //create a new list with the old favourite buses and add the new bus
                                    ArrayList<String> newUserList = new ArrayList<>();
                                    for(MenuBus bus : allbuses){
                                        if(bus.isFavourite()){
                                            newUserList.add(bus.getRoute());
                                        }
                                    }
                                    newUserList.add("Default");
                                    newUserList.add(selectedBoi.getRoute());
                                    usersnapshot.getRef().child("bois").setValue(newUserList);
                                    //make the bus a favourite
                                    for(MenuBus menuBus : allbuses){
                                        if(menuBus.getRoute().equals(selectedBoi.getRoute())){
                                            menuBus.setFavourite(true);
                                            Collections.sort(allbuses);
                                            boiAdapter.notifyDataSetChanged();
                                            break;
                                        }
                                    }
                                    boiAdapter.notifyDataSetChanged();
                                }
                            }
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
            }

            @Override
            public void onCustomItemClickListener(AdapterView<?> parent, View view, final int position) {
                final String route = ((MenuBus)parent.getItemAtPosition(position)).getRoute()+"";
                final String busnumber = ((MenuBus)parent.getItemAtPosition(position)).getBusNumber()+"";
                ref.child("Bus").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot busSnapshot : snapshot.getChildren()){
                            if(busSnapshot.child("bus_route").getValue(String.class).equals(route)){
                                isOnline = busSnapshot.child("online").getValue(boolean.class);
                                isRequested = busSnapshot.child("requested").getValue(boolean.class);
                                isShared = busSnapshot.child("p2pshared").getValue(boolean.class);
                            }
                        }
                        if(!route.equals("Select a bus")) {
                            //check if the bus is online to send the intent
                            if (isOnline) {
                                //Obtain the info of the bus (route, number and driver number) from the drives table
                                //and send them in an intent to the UserMap activity
                                final takes intentTakes = new takes();
                                ref.child("drives").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot snapshot) {
                                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                            if (mFirebaseAuth.getCurrentUser()!=null && dataSnapshot.child("busroute").getValue(String.class).equals(route)) {
                                                intentTakes.setPhonenumber(dataSnapshot.child("phonenumber").getValue(String.class));
                                                intentTakes.setBusroute(route);
                                                intentTakes.setBusnumber(busnumber);
                                                intentTakes.setUser_email(mFirebaseAuth.getCurrentUser().getEmail());
                                                Intent inttousermap = new Intent(UserMenuActivity.this, UserMap.class);
                                                inttousermap.putExtra("intentTakes", intentTakes);
                                                startActivity(inttousermap);
                                            }
                                        }
                                    }
                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {}
                                });
                            }
                            //If the bus is offline, check if it's p2pshared or not
                            //if it's offline and shared, send the user to the p2pMapShared
                            else if(!isOnline && isShared){
                                //send the user to a map activity that contains only the bus route, number and a "where's my bus?" button
                                final p2pshare intentp2pshare = new p2pshare();
                                ref.child("p2pshare").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot snapshot) {
                                        for (DataSnapshot p2psnapshot : snapshot.getChildren()) {
                                            if (p2psnapshot.child("busroute").getValue(String.class).equals(route)) {
                                                //send data in intent: bus number, route and sharer's email to
                                                //be able to retrieve their location.
                                                intentp2pshare.setEmail(p2psnapshot.child("email").getValue(String.class));
                                                intentp2pshare.setBusroute(p2psnapshot.child("busroute").getValue(String.class));
                                                intentp2pshare.setBusnumber(p2psnapshot.child("busnumber").getValue(String.class));
                                                if(mFirebaseAuth.getCurrentUser()!=null && p2psnapshot.child("email").getValue(String.class).equals(mFirebaseAuth.getCurrentUser().getEmail())){
                                                    Intent inttouserp2phostmap = new Intent(UserMenuActivity.this, Userp2phostmap.class);
                                                    inttouserp2phostmap.putExtra("intentp2pshare", intentp2pshare);
                                                    startActivity(inttouserp2phostmap);
                                                }
                                                else{
                                                    Intent inttouserp2psharemap = new Intent(UserMenuActivity.this, Userp2psharemap.class);
                                                    inttouserp2psharemap.putExtra("intentp2pshare", intentp2pshare);
                                                    startActivity(inttouserp2psharemap);
                                                }
                                            }
                                        }
                                    }
                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {}
                                });
                            }
                            //if it's offline and not shared but requested? tell the user this location is
                            //already requested and ask them if they want to share or not
                            else if(!isOnline && !isShared && isRequested){
                                new AlertDialog.Builder(UserMenuActivity.this)
                                        .setTitle("Woops!")
                                        .setMessage("This bus's location is being requested by a peer. Are you on the bus and would like to share its location?")

                                        // Specifying a listener allows you to take an action before dismissing the dialog.
                                        // The dialog is automatically dismissed when a dialog button is clicked.
                                        .setPositiveButton("Yes, please", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                //send the user to a new map that has a start/stop sharing, bus number and bus route
                                                p2pshare p2phost = new p2pshare();
                                                p2phost.setEmail(mFirebaseAuth.getCurrentUser().getEmail());
                                                p2phost.setBusnumber(busnumber);
                                                p2phost.setBusroute(route);
                                                Intent inttop2phostmap = new Intent(UserMenuActivity.this, Userp2phostmap.class);
                                                inttop2phostmap.putExtra("intentp2pshare", p2phost);
                                                startActivity(inttop2phostmap);
                                            }
                                        })
                                        // A null listener allows the button to dismiss the dialog and take no further action.
                                        .setNegativeButton("No, thanks", null)
                                        .setIcon(android.R.drawable.ic_dialog_alert)
                                        .show();
                            }
                            //if it's offline, not shared and not requested, give the user a prompt asking
                            //them if they want to send a request for a user to broadcast the bus's location
                            else if(!isOnline && !isShared && !isRequested){
                                new AlertDialog.Builder(UserMenuActivity.this)
                                        .setTitle("Woops!")
                                        .setMessage("This bus appears to be offline. Would you like to request its location to be shared by a fellow peer that might be on the bus?")

                                        // Specifying a listener allows you to take an action before dismissing the dialog.
                                        // The dialog is automatically dismissed when a dialog button is clicked.
                                        .setPositiveButton("Yes, please", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                //set the bus's isRequested to true
                                                //get the bus from the database and change the requested value to true
                                                ref.child("Bus").addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                        for(DataSnapshot busSnapshot : snapshot.getChildren()){
                                                            if(busSnapshot.child("bus_route").getValue(String.class).equals(route)){
                                                                busSnapshot.getRef().child("requested").setValue(true);
                                                                isRequested = true;
                                                                Toast.makeText(UserMenuActivity.this, "Request successfully sent", Toast.LENGTH_SHORT).show();
                                                                boiAdapter.notifyDataSetChanged();
                                                            }
                                                        }
                                                    }
                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError error) {}
                                                });
                                            }
                                        })
                                        // A null listener allows the button to dismiss the dialog and take no further action.
                                        .setNegativeButton("No, thanks", null)
                                        .setIcon(android.R.drawable.ic_dialog_alert)
                                        .show();
                            }
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
            }
        });
        favourites.setAdapter(boiAdapter);

        //Notification part
        ref.child("Bus").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot busSnapShot : dataSnapshot.getChildren()){
                    final String busRoute = busSnapShot.child("bus_route").getValue(String.class);
                    if(busSnapShot.child("online").getValue(Boolean.class)){
                        //check if the bus is in the bois list and if so, send an online notification
                        ref.child("Passengers").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for(DataSnapshot usersnapshot : snapshot.getChildren()){
                                    if(mFirebaseAuth.getCurrentUser()!=null && usersnapshot.child("email").getValue(String.class).equals(mFirebaseAuth.getCurrentUser().getEmail())){
                                        for(DataSnapshot boissnapshot : usersnapshot.child("bois").getChildren()){
                                            route = busRoute;
                                            if(boissnapshot.getValue(String.class).equals(route)){
                                                createOnlineNotification();
                                            }
                                        }
                                    }
                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                            }
                        });
                    }
                    if(busSnapShot.child("requested").getValue(Boolean.class)){
                        //check if the bus is in the bois list and if so, send a "share request" notification
                        ref.child("Passengers").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for(DataSnapshot usersnapshot : snapshot.getChildren()){
                                    if(mFirebaseAuth.getCurrentUser()!=null && usersnapshot.child("email").getValue(String.class).equals(mFirebaseAuth.getCurrentUser().getEmail())){
                                        for(DataSnapshot boissnapshot : usersnapshot.child("bois").getChildren()){
                                            route = busRoute;
                                            if(boissnapshot.getValue(String.class).equals(route)){
                                                createShareRequestNotification();
                                            }
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
            }
            @Override
            public void onCancelled( DatabaseError error) {}
        });
    }

    private void createShareRequestNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, SHARE_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_bus_24)
                .setContentTitle("GUC Bus location request!")
                .setContentText("A user is requesting for the location of bus route " + route +" to be shared.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(2, builder.build());
    }

    private void createOnlineNotification(){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, ONLINE_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_bus_24)
                .setContentTitle("GUC Bus online!")
                .setContentText("Bus of route " + route + " is now online and moving!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(1, builder.build());
    }

    //function to create a channel for notifications
    private void createOnlineNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.online_channel_name);
            String description = getString(R.string.online_channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(ONLINE_CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
    private void createShareNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.share_channel_name);
            String description = getString(R.string.share_channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(SHARE_CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
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
            new AlertDialog.Builder(UserMenuActivity.this)
                    .setTitle("Logout?")
                    // Specifying a listener allows you to take an action before dismissing the dialog.
                    // The dialog is automatically dismissed when a dialog button is clicked.
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            final String curEmail = mFirebaseAuth.getCurrentUser().getEmail();
                            final String[] route = {""};
                            //TODO check if the user is p2psharing location and stop that as well
                            ref.child("p2pshare").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    for (DataSnapshot takesSnapshot : snapshot.getChildren()) {
                                        if (takesSnapshot.child("email").getValue(String.class).equals(curEmail)) {
                                            route[0] = takesSnapshot.child("busroute").getValue(String.class);
                                            ref.child("Bus").addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot busdataSnapshot) {
                                                    //loop over all children of the "Bus" table
                                                    for (DataSnapshot bussnapshot : busdataSnapshot.getChildren()) {
                                                        //check the current datasnapshot if it has the same route as the shared bus
                                                        if (bussnapshot.child("bus_route").getValue(String.class).equals(route[0])){
                                                            //if so, make that new bus offline
                                                            bussnapshot.getRef().child("p2pshared").setValue(false);
                                                        }
                                                    }
                                                }
                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {}
                                            });

                                            takesSnapshot.getRef().child("busnumber").setValue("nothing");
                                            takesSnapshot.getRef().child("busroute").setValue("nothing");
                                            takesSnapshot.getRef().child("buslat").setValue(0);
                                            takesSnapshot.getRef().child("buslon").setValue(0);
                                            break;
                                        }
                                    }
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                }
                            });
                            //stop the user from sharing their location before logging them out
                            ref.child("takes").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    for (DataSnapshot takesSnapshot : snapshot.getChildren()) {
                                        if (takesSnapshot.child("user_email").getValue(String.class).equals(curEmail)) {
                                            takesSnapshot.getRef().child("busnumber").setValue("nothing");
                                            takesSnapshot.getRef().child("busroute").setValue("nothing");
                                            takesSnapshot.getRef().child("phonenumber").setValue("nothing");
                                            //set the location to be (0,0) to be out of the driver's vision
                                            takesSnapshot.getRef().child("user_lat").setValue(0);
                                            takesSnapshot.getRef().child("user_lon").setValue(0);
                                            break;
                                        }
                                    }
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                }
                            });
                            mFirebaseAuth.signOut();
                            Intent inttologin = new Intent(UserMenuActivity.this, ChooseTypeActivity.class);
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
