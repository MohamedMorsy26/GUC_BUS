package com.example.herewego;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

public class AdminViewUsersActivity extends AppCompatActivity {
    private RecyclerView mRecyclerView;
    private UserAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    public final ArrayList<user> userList = new ArrayList<>();
    public final ArrayList<user> filteredUserList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adminviewusers);

        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference();

        ref.child("Passengers").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for(DataSnapshot userSnapshot: snapshot.getChildren()){
                    user u = new user();
                    u.setEmail(userSnapshot.child("email").getValue(String.class));
                    u.setFirstname(userSnapshot.child("firstname").getValue(String.class));
                    u.setLastname(userSnapshot.child("lastname").getValue(String.class));
                    userList.add(u);
                    mAdapter.notifyDataSetChanged();
                }
                filteredUserList.clear();
                filteredUserList.addAll(userList);
                Collections.sort(filteredUserList);
                mAdapter.clearItems();
                mAdapter.addItems(filteredUserList);
                mAdapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        mRecyclerView = findViewById(R.id.recyclerviewid);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mAdapter = new UserAdapter(filteredUserList);

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setCustomButtonClickListener(new UserAdapter.customButtonClickListener() {
            @Override
            public void onEnableButtonClickListener(int position) {
                final String email = filteredUserList.get(position).getEmail();
                ref.child("Passengers").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot userSnapshot : snapshot.getChildren()){
                            if(userSnapshot.child("email").getValue(String.class).equals(email)){
                                userSnapshot.getRef().child("enabled").setValue(true);
                            }
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
            }

            @Override
            public void onDisableButtonClickListener(int position) {
                final String email = filteredUserList.get(position).getEmail();
                ref.child("Passengers").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot userSnapshot : snapshot.getChildren()){
                            if(userSnapshot.child("email").getValue(String.class).equals(email)){
                                userSnapshot.getRef().child("enabled").setValue(false);
                            }
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
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
                mAdapter.clearItems();
                mAdapter.addItems(userList);
                mAdapter.getFilter().filter(searchText);

                return false;
            }
        });
        return true;
    }
}
