package com.barlis.chat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.barlis.chat.Fragments.APIService;
import com.barlis.chat.Fragments.ChatsFragment;
import com.barlis.chat.Fragments.ProfileFragment;
import com.barlis.chat.Fragments.RequestsFragment;
import com.barlis.chat.Fragments.UsersFragment;
import com.barlis.chat.Model.Chat;
import com.barlis.chat.Model.ERequestCodes;
import com.barlis.chat.Model.ERequestStatus;
import com.barlis.chat.Model.EResultCodes;
import com.barlis.chat.Model.Request;
import com.barlis.chat.Model.User;
import com.barlis.chat.Notification.Client;
import com.barlis.chat.Notification.Data;
import com.barlis.chat.Notification.MyResponse;
import com.barlis.chat.Notification.NotificationBuilder;
import com.barlis.chat.Notification.Sender;
import com.barlis.chat.Notification.Token;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    final int LOCATION_PERMISSION_REQUEST = ERequestCodes.LOCATION_PERMISSION.getValue();

    private CircleImageView profile_image_civ;
    private TextView username_tv;
    private TabLayout tabLayout;
    private UsersFragment usersFragment;
    private RequestsFragment requestsFragment;
    FirebaseUser firebaseUser;
    DatabaseReference databaseReference;

    //Start Hanan part
    CoordinatorLayout coordinatorLayout;
    FloatingActionButton floatingBtn;
    String profession = "",job_description = "",note = "",qualifications = "";//looking for
    private Uri imageUri = null;
    //End Hanan part

    Location userLocation;
    boolean gotUserLocation = false;
    int maxDistance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        // Set empty user location
        userLocation = new Location("");
        usersFragment = new UsersFragment();
        requestsFragment = new RequestsFragment();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

        coordinatorLayout = findViewById(R.id.coordinator);

        floatingBtn = findViewById(R.id.floatingBtn);
        floatingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent popUpIntent = new Intent(getApplicationContext(), PopUpActivity.class);
                startActivityForResult(popUpIntent, ERequestCodes.NEW_REQUEST.getValue());
            }
        });

        profile_image_civ = findViewById(R.id.profile_image);
        username_tv = findViewById(R.id.username);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

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

                if (dataSnapshot.hasChild("latitude")) { // Set last known location if it exists
                    userLocation.setLatitude(dataSnapshot.child("latitude").getValue(double.class));
                    userLocation.setLongitude(dataSnapshot.child("longitude").getValue(double.class));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        tabLayout = findViewById(R.id.tab_layout);

        ViewPager viewPager = findViewById(R.id.view_pager);

        databaseReference = FirebaseDatabase.getInstance().getReference("Chats");
        databaseReference.addValueEventListener(new ValueEventListener() { // reference to the chats of the current user
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());

                int unreadMessages = 0;

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Chat chat = snapshot.getValue(Chat.class);

                    if (chat.getReceiver().equals(firebaseUser.getUid()) && !chat.isIsseen()) { // Counts some unread messages
                            unreadMessages++;
                    }
                }

                if (unreadMessages == 0) {
                    viewPagerAdapter.addFragment(new ChatsFragment(), getResources().getString(R.string.chats_tab));
                }
                else { // If there are some unread messages then it presents it in the tab
                    viewPagerAdapter.addFragment(new ChatsFragment(), "(" + unreadMessages + ") " + getResources().getString(R.string.chats_tab));
                }

                viewPagerAdapter.addFragment(usersFragment, getResources().getString(R.string.users_tab));
                viewPagerAdapter.addFragment(requestsFragment, getResources().getString(R.string.requests_tab));
                viewPagerAdapter.addFragment(new ProfileFragment(), getResources().getString(R.string.profile_tab));
                viewPager.setAdapter(viewPagerAdapter);

                tabLayout.setupWithViewPager(viewPager);
                tabLayout.getTabAt(0).setIcon(R.drawable.chat);
                tabLayout.getTabAt(1).setIcon(R.drawable.group);
                tabLayout.getTabAt(2).setIcon(R.drawable.requests);
                tabLayout.getTabAt(3).setIcon(R.drawable.profile);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        if (Build.VERSION.SDK_INT >= 23) { // Check for location permission
            int hasLocationPermission = checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);
            if (hasLocationPermission != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION},LOCATION_PERMISSION_REQUEST);
            }
            else getLocation();
        }
        else getLocation();

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
    public boolean onCreateOptionsMenu(Menu menu) { // Creates the menu
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) { // The menu buttons
        switch (item.getItemId()){
            case R.id.my_requests:
                startActivity(new Intent(MainActivity.this, UserSpecificRequestsActivity.class).putExtra("user_name", username_tv.getText().toString()));
                return true;
            case R.id.logout:
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(MainActivity.this, StartActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                return true;
        }
        return false;
    }


    class ViewPagerAdapter extends FragmentPagerAdapter{

        private ArrayList<Fragment> fragments;
        private ArrayList<String> titles;

        ViewPagerAdapter (FragmentManager fm){
            super(fm);
            this.fragments = new ArrayList<>();
            this.titles = new ArrayList<>();
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        public void addFragment(Fragment fragment, String title){
            fragments.add(fragment);
            titles.add(title);
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return titles.get(position);
        }
    }

    private void userStatus(String status){ // Changes the user's status to offline or online
        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status", status);

        databaseReference.updateChildren(hashMap);
    }

    // Tom
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Getting information back from popUp activity
        if (requestCode == ERequestCodes.NEW_REQUEST.getValue()) { // Getting information back from popUp activity
            if (resultCode == EResultCodes.SEARCH_USER.getValue()) {
                if (data.hasExtra("profession")) {
                    profession = data.getStringExtra("profession");
                }
                if (data.hasExtra("distance")) {
                    maxDistance = data.getIntExtra("distance", 0) * 1000;
                }
                // Search for users and move to the users tab
                tabLayout.getTabAt(1).select();
                usersFragment.setNewSearchQuery(profession, data.getBooleanExtra("searchByDistance", false), userLocation, maxDistance);
            }
            else if (resultCode == EResultCodes.OPEN_REQUEST.getValue()) {
                // Open new Request
                Request request = (Request) data.getSerializableExtra("request");
                request.setCreatorId(firebaseUser.getUid());
                request.setCreatorName(username_tv.getText().toString());
                requestsFragment.addRequest(request);
            }
        }
        else if (requestCode == ERequestCodes.UPDATE_REQUEST.getValue()) {
            if (resultCode == EResultCodes.UPDATE_REQUEST_WORKER.getValue()) {
                // Add worker to request and send that user a notification
                requestsFragment.updateRequestWorker(data.getIntExtra("request_position", 0), username_tv.getText().toString(), firebaseUser.getUid());
                Intent intent = new Intent(MainActivity.this, MessageActivity.class);
                intent.putExtra("userId", data.getStringExtra("creatorId"));
                startActivity(intent);
                NotificationBuilder.sendNotification(this, firebaseUser.getUid(),data.getStringExtra("creatorId"), getResources().getString(R.string.job_taken_message_body), username_tv.getText().toString() + " " + getResources().getString(R.string.job_taken_notification));
            }
            else if (resultCode == EResultCodes.REMOVE_WORKER.getValue()) {
                // Remove worker from request and send that user a notification
                requestsFragment.removeWorkerFromRequest(data.getIntExtra("request_position", 0), data.getStringExtra("workerId"));
                NotificationBuilder.sendNotification(this, firebaseUser.getUid(), data.getStringExtra("workerId"), username_tv.getText().toString() + " " + getResources().getString(R.string.removed_from_request_alert), getResources().getString(R.string.request_update_alert));
            }
            else if (resultCode == EResultCodes.QUIT_REQUEST.getValue()) {
                // Remove worker from request and send the creator a notification
                requestsFragment.removeWorkerFromRequest(data.getIntExtra("request_position", 0), firebaseUser.getUid());
                NotificationBuilder.sendNotification(this, firebaseUser.getUid(), data.getStringExtra("creatorId"), username_tv.getText().toString() + " " + getResources().getString(R.string.worker_quit_alert), getResources().getString(R.string.request_update_alert));
            }
            else if (resultCode == EResultCodes.CLOSE_REQUEST.getValue()) {
                // Close request and send the worker a notification
                requestsFragment.closeRequest(data.getIntExtra("request_position",0));
                NotificationBuilder.sendNotification(this, firebaseUser.getUid(), data.getStringExtra("workerId"), username_tv.getText().toString() + " " + getResources().getString(R.string.request_closed_alert), getResources().getString(R.string.request_update_alert));
            }
        }
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
}
