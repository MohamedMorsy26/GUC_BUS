package com.example.herewego;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

public class AdminViewLogsActivity extends AppCompatActivity {

    RecyclerView mRecyclerView;
    Button clearLogsButton;
    ArrayList<logItem> logsList = new ArrayList<>();
    ArrayList<logItem> filteredLogList = new ArrayList<>();
    private LogAdapter mLogAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adminviewlogs);
        mRecyclerView = findViewById(R.id.logsrecyclerviewid);
        clearLogsButton = findViewById(R.id.adminclearlogsbuttonid);
        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference();

        ref.child("Logs").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                logsList.clear();
                for(DataSnapshot logsSnapshot : snapshot.getChildren()){
                    logItem item = new logItem();

                    item.setText(logsSnapshot.child("text").getValue(String.class));
                    item.setDay(logsSnapshot.child("day").getValue(String.class));
                    item.setMonth(logsSnapshot.child("month").getValue(String.class));
                    item.setYear(logsSnapshot.child("year").getValue(String.class));
                    item.setHour(logsSnapshot.child("hour").getValue(String.class));
                    item.setMinute(logsSnapshot.child("minute").getValue(String.class));
                    if(!logsSnapshot.child("text").getValue(String.class).equals("Rejected")){
                        logsList.add(item);
                    }
                    mLogAdapter.notifyDataSetChanged();
                }
                filteredLogList.clear();
                filteredLogList.addAll(logsList);
                Collections.sort(filteredLogList);
                mLogAdapter.clearItems();
                mLogAdapter.addAllItems(filteredLogList);
                mLogAdapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        mRecyclerView.setHasFixedSize(true);
        mLogAdapter = new LogAdapter(filteredLogList);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mLogAdapter);

        clearLogsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //remove all entries in the database whose text isn't "Rejected"
                new AlertDialog.Builder(AdminViewLogsActivity.this)
                        .setTitle("Clear all logs?")

                        // Specifying a listener allows you to take an action before dismissing the dialog.
                        // The dialog is automatically dismissed when a dialog button is clicked.
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                //set the bus's isRequested to true
                                //get the bus from the database and change the requested value to true
                                ref.child("Logs").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        for(DataSnapshot logsSnapshot : snapshot.getChildren()){
                                            if(!logsSnapshot.child("text").getValue(String.class).equals("Rejected")){
                                                logsSnapshot.getRef().setValue(null);
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                                //clear the logs in the arraylists and notify the adapter with the changes
                                logsList.clear();
                                filteredLogList.clear();
                                mLogAdapter.notifyDataSetChanged();
                            }
                        })
                        // A null listener allows the button to dismiss the dialog and take no further action.
                        .setNegativeButton("No", null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.user_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.search_icon);
        androidx.appcompat.widget.SearchView searchView = (androidx.appcompat.widget.SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(final String searchText) {

                mLogAdapter.clearItems();
                mLogAdapter.addAllItems(logsList);
                mLogAdapter.getFilter().filter(searchText);

                return false;
            }
        });
        return true;
    }
}