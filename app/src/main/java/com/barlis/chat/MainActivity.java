package com.barlis.chat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.barlis.chat.Fragments.ChatsFragment;
import com.barlis.chat.Fragments.HomeScreenFragment;
import com.barlis.chat.Fragments.ProfileFragment;
import com.barlis.chat.Fragments.RequestsFragment;
import com.barlis.chat.Fragments.UsersFragment;
import com.barlis.chat.Model.Chat;
import com.barlis.chat.Model.ERequestCodes;
import com.barlis.chat.Model.EResultCodes;
import com.barlis.chat.Model.Request;
import com.barlis.chat.Model.User;
import com.barlis.chat.Notification.NotificationBuilder;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    final int LOCATION_PERMISSION_REQUEST = ERequestCodes.LOCATION_PERMISSION.getValue();
    final String HOME_SCREEN_TAG = "HOME_SCREEN_TAG";
    final String MY_REQUESTS_TAG = "MY_REQUESTS_TAG";

    private CircleImageView profile_image_civ;
    private TextView username_tv;
    private DrawerLayout drawerLayout;
    private HomeScreenFragment homeScreenFragment;
    private UserSpecificRequestsFragment userSpecificRequestsFragment;
    private int currentFragment = 1;

    FirebaseUser firebaseUser;
    DatabaseReference databaseReference;



    Location userLocation;
    boolean gotUserLocation = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set empty user location
        userLocation = new Location("");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu);
        NavigationView navigationView = findViewById(R.id.navigation_view);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {// The menu buttons
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.home_screen:
                        getSupportFragmentManager().beginTransaction().show(getSupportFragmentManager().findFragmentByTag(HOME_SCREEN_TAG)).commit();
                        if (getSupportFragmentManager().findFragmentByTag(MY_REQUESTS_TAG) != null) {
                            getSupportFragmentManager().beginTransaction().hide(getSupportFragmentManager().findFragmentByTag(MY_REQUESTS_TAG)).commit();
                        }
                        currentFragment = 1;
                        break;
                    case R.id.my_requests:
                        if (getSupportFragmentManager().findFragmentByTag(MY_REQUESTS_TAG) != null) {
                            getSupportFragmentManager().beginTransaction().show(getSupportFragmentManager().findFragmentByTag(MY_REQUESTS_TAG)).commit();
                        }
                        else {
                            getSupportFragmentManager().beginTransaction().add(R.id.fragment_layout ,userSpecificRequestsFragment, MY_REQUESTS_TAG).commit();
                        }
                        getSupportFragmentManager().beginTransaction().hide(getSupportFragmentManager().findFragmentByTag(HOME_SCREEN_TAG)).commit();
                        currentFragment = 2;
                        break;
                    case R.id.logout:
                        FirebaseAuth.getInstance().signOut();
                        startActivity(new Intent(MainActivity.this, StartActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                        break;
                }
                drawerLayout.closeDrawers();
                return true;
            }
        });
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        homeScreenFragment = new HomeScreenFragment();
        homeScreenFragment.setFirebaseUser(firebaseUser);
        userSpecificRequestsFragment = new UserSpecificRequestsFragment();

        profile_image_civ = findViewById(R.id.profile_image);
        username_tv = findViewById(R.id.username);

        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        databaseReference.addValueEventListener(new ValueEventListener() { // reference to the information of the current user
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);

                if (user != null) // If the user's information is empty
                    System.out.println("user profession: " + user.getProfession());

                username_tv.setText(user.getUsername());

                if (user.getImageURL().equals("default")) {  // When there is no link to the image use the default
                    profile_image_civ.setImageResource(R.drawable.students_icon);
                }
                else {
                    Picasso.get().load(user.getImageURL()).into(profile_image_civ);
                }


                homeScreenFragment.setUserName(username_tv.getText().toString());
                userSpecificRequestsFragment.setUserName(username_tv.getText().toString());
                if (dataSnapshot.hasChild("latitude")) { // Set last known location if it exists
                    userLocation.setLatitude(dataSnapshot.child("latitude").getValue(double.class));
                    userLocation.setLongitude(dataSnapshot.child("longitude").getValue(double.class));
                    homeScreenFragment.setUserLocation(userLocation);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



        if (Build.VERSION.SDK_INT >= 23) { // Check for location permission
            int hasLocationPermission = checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);
            if (hasLocationPermission != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST);
            }
            else getLocation();
        }
        else getLocation();

        getSupportFragmentManager().beginTransaction().add(R.id.fragment_layout, homeScreenFragment, HOME_SCREEN_TAG).commit();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==LOCATION_PERMISSION_REQUEST) {
            // If user approved then get location data
            if(grantResults[0]==PackageManager.PERMISSION_GRANTED) {
                getLocation();
            }
        }
    }

    private void getLocation() {
        FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(this);

        // Get location every 30 seconds
        LocationRequest request =LocationRequest.create();
        request.setInterval(30 * 1000);
        request.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        LocationCallback callback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                Location location = locationResult.getLastLocation();
                // Get first time location
                if (!gotUserLocation) {
                    userLocation.setLatitude(location.getLatitude());
                    userLocation.setLongitude(location.getLongitude());
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("latitude", location.getLatitude());
                    map.put("longitude", location.getLongitude());
                    databaseReference.updateChildren(map);
                    gotUserLocation = true;
                }
                // Only update db when user traveled more than 5 meters
                if (userLocation.distanceTo(location) > 5) {
                    userLocation.setLatitude(location.getLatitude());
                    userLocation.setLongitude(location.getLongitude());
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("latitude", location.getLatitude());
                    map.put("longitude", location.getLongitude());
                    databaseReference.updateChildren(map);
                }
            }
        };
        if(Build.VERSION.SDK_INT>=23 && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED) {
            client.requestLocationUpdates(request, callback, null);
        }
    }



    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            drawerLayout.openDrawer(GravityCompat.START);
        }

        return super.onOptionsItemSelected(item);
    }




    private void userStatus(String status){ // Changes the user's status to offline or online
        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status", status);

        databaseReference.updateChildren(hashMap);
    }

    @Override
    protected void onResume() { // When you return to the app then the status changes to online
        super.onResume();
        userStatus("online");
    }

    @Override
    protected void onPause() { // When leaving the app, the status changes to offline
        super.onPause();
        userStatus("offline");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        // Getting information back from popUp activity
        if (requestCode == ERequestCodes.NEW_REQUEST.getValue()) { // Getting information back from popUp activity
            if (resultCode == EResultCodes.SEARCH_USER.getValue()) {
                String profession = "";
                int maxDistance = 0;
                if (data.hasExtra("profession")) {
                    profession = data.getStringExtra("profession");
                }
                if (data.hasExtra("distance")) {
                    maxDistance = data.getIntExtra("distance", 0) * 1000;
                }
                // Search for users and move to the users tab
                homeScreenFragment.searchUsers(profession, data.getBooleanExtra("searchByDistance", false), maxDistance);
            }
            else if (resultCode == EResultCodes.OPEN_REQUEST.getValue()) {
                // Open new Request
                homeScreenFragment.openRequest((Request) data.getSerializableExtra("request"));
            }
        }
        else if (requestCode == ERequestCodes.UPDATE_REQUEST.getValue()) {
            if (resultCode == EResultCodes.UPDATE_REQUEST_WORKER.getValue()) {
                // Add worker to request and send that user a notification
                homeScreenFragment.addWorkerToRequest(data.getIntExtra("request_position", 0), data.getStringExtra("creatorId"));
            }
            else if (resultCode == EResultCodes.REMOVE_WORKER.getValue()) {
                // Remove worker from request and send that user a notification
                if (currentFragment == 1) {
                    homeScreenFragment.removeWorkerFromRequest(data.getIntExtra("request_position", 0), data.getStringExtra("workerId"));
                }
                else {
                    userSpecificRequestsFragment.removeWorkerFromRequest(data.getIntExtra("request_position", 0), data.getStringExtra("workerId"));
                }
            }
            else if (resultCode == EResultCodes.QUIT_REQUEST.getValue()) {
                // Remove worker from request and send the creator a notification
                if (currentFragment == 1) {
                    homeScreenFragment.quitFromRequest(data.getIntExtra("request_position", 0), firebaseUser.getUid());
                }
                else {
                    userSpecificRequestsFragment.quitFromRequest(data.getIntExtra("request_position", 0), firebaseUser.getUid());
                }
            }
            else if (resultCode == EResultCodes.CLOSE_REQUEST.getValue()) {
                // Close request and send the worker a notification
                if (currentFragment == 1) {
                    homeScreenFragment.closeRequest(data.getIntExtra("request_position",0), data.getStringExtra("workerId"));
                }
                else {
                    userSpecificRequestsFragment.closeRequest(data.getIntExtra("request_position",0), data.getStringExtra("workerId"));
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
