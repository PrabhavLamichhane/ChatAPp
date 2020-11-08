package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNav;
    DatabaseReference reference;
    FirebaseUser firebaseUser;

    TextView frags;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        frags = findViewById(R.id.frags);
        bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListen);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");


        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();


        //I added this if statement to keep the selected fragment when rotating the device
        if (savedInstanceState == null) {
            frags.setText("PEOPLE");
            bottomNav.setSelectedItemId(R.id.people);
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new PeopleFragment()).commit();
        }




    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListen =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Fragment selectedFragment = null;

                    switch (item.getItemId()) {
                        case R.id.chats:
                            frags.setText("CHATS");
                            selectedFragment = new ChatsFragment();
                            break;
                        case R.id.people:
                            frags.setText("PEOPLE");
                            selectedFragment = new PeopleFragment();
                            break;
                        case R.id.settings:
                            frags.setText("SETTINGS");
                            selectedFragment = new SettingsFragment();
                            break;
                    }
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                            selectedFragment).commit();
                    return true;
                }
            };

    @Override
    protected void onStart() {
        super.onStart();

//        set status online
        status("online");
    }

    @Override
    protected void onResume() {
        super.onResume();

//        set status online
        status("online");
    }

    @Override
    protected void onStop() {
        super.onStop();
        status("offline");

//        set status offline
    }

    public void status(String status){
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("status",status);

        reference.updateChildren(hashMap);
    }


    @Override
    protected void onPause() {
        super.onPause();
        status("online");
    }
}
