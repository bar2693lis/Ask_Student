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

import com.barlis.chat.Fragments.ChatsFragment;
import com.barlis.chat.Fragments.ProfileFragment;
import com.barlis.chat.Fragments.UsersFragment;
import com.barlis.chat.Model.Chat;
import com.barlis.chat.Model.User;
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
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    final int LOCATION_PERMISSION_REQUEST = 1;
    final int NEW_JOB_REQUEST_CODE = 2;

    CircleImageView profileImage;
    TextView username;

    //Start Hanan part
    CoordinatorLayout coordinatorLayout;
    FloatingActionButton floatingBtn;
    String profession = "",job_description = "",note = "",qualifications = "";//looking for
   // String userProfession,userQualification,userExperience,userPersonal;
    private Uri imageUri = null;
    //End Hanan part
    Location userLocation;
    int maxDistance;
    TabLayout tabLayout;
    FirebaseUser firebaseUser;
    DatabaseReference reference;
    private UsersFragment usersFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*if(getIntent().getBooleanExtra("employer",true))
        {
            setContentView(R.layout.employer_layout);
        }*/




        //else {
            setContentView(R.layout.activity_main);
            userLocation = new Location("");
            usersFragment = new UsersFragment();

            Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            getSupportActionBar().setTitle("");
            coordinatorLayout = findViewById(R.id.coordinator);
            floatingBtn = findViewById(R.id.floatingBtn);
            floatingBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent popUpIntent = new Intent(getApplicationContext(), PopUpActivity.class);
                    startActivityForResult(popUpIntent, NEW_JOB_REQUEST_CODE);
                }
            });
        /*if(getIntent().hasExtra("userProfession"))
        {//from profile activity - after registration with email part
            userProfession = getIntent().getStringExtra("userProfession");
            userQualification = getIntent().getStringExtra("userQualification");
            userPersonal = getIntent().getStringExtra("personal");
            userExperience = getIntent().getStringExtra("userExperience");
            if(getIntent().hasExtra("imageUri"))
                imageUri = getIntent().getParcelableExtra("imageUri");
        }*/


            profileImage = findViewById(R.id.profile_image);
            username = findViewById(R.id.username);

            firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    User user = dataSnapshot.getValue(User.class);
                    if (user != null)
                        System.out.println("user profession: " + user.getProfession());
                /*if(user.getEmployee()==null)
                    floatingBtn.hide();
                else
                    floatingBtn.show();*/
                    username.setText(user.getUsername());

                    if (user.getImageURL().equals("default")) {
                        profileImage.setImageResource(R.mipmap.ic_launcher);
                    } else {
                        Picasso.get().load(user.getImageURL()).into(profileImage);
                    }
                    // Tom
                    if (dataSnapshot.hasChild("latitude")) {
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

            reference = FirebaseDatabase.getInstance().getReference("Chats");
            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());

                    int unreadMessages = 0;

                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Chat chat = snapshot.getValue(Chat.class);

                        if (chat.getReceiver().equals(firebaseUser.getUid()) && !chat.isIsseen()) {
                            unreadMessages++;
                        }
                    }

                    if (unreadMessages == 0) {
                        viewPagerAdapter.addFragment(new ChatsFragment(), "Chats");
                    } else {
                        viewPagerAdapter.addFragment(new ChatsFragment(), "(" + unreadMessages + ") Chats");
                    }

                    //viewPagerAdapter.addFragment(new UsersFragment(), "Users");
                    viewPagerAdapter.addFragment(usersFragment, "Users");
                    viewPagerAdapter.addFragment(new ProfileFragment(), "Profile");

                    viewPager.setAdapter(viewPagerAdapter);

                    tabLayout.setupWithViewPager(viewPager);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });

            if (Build.VERSION.SDK_INT >= 23) {
                int hasLocationPermission = checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);
                if (hasLocationPermission != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION},LOCATION_PERMISSION_REQUEST);
                }
                else getLocation();
            }
            else getLocation();

        }
    //}

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==LOCATION_PERMISSION_REQUEST) {
            if(grantResults[0]!=PackageManager.PERMISSION_GRANTED) {
                // User denied permission
            }
            else getLocation();
        }
    }

    private void getLocation() {
        FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(this);

        LocationRequest request =LocationRequest.create();
        request.setInterval(5000);
        request.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        LocationCallback callback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                Location location = locationResult.getLastLocation();
                userLocation.setLatitude(location.getLatitude());
                userLocation.setLongitude(location.getLongitude());
                HashMap<String, Object> map = new HashMap<>();
                map.put("latitude", location.getLatitude());
                map.put("longitude", location.getLongitude());
                reference.updateChildren(map);
            }
        };

        if(Build.VERSION.SDK_INT>=23 && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED) {
            client.requestLocationUpdates(request, callback, null);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){

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

    private void userStatus(String status){
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status", status);

        reference.updateChildren(hashMap);
    }

    // Tom
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Getting information back from popUp activity
        if (requestCode == NEW_JOB_REQUEST_CODE) {
            if (resultCode == 2) {
                if (data.hasExtra("profession")) {
                    profession = data.getStringExtra("profession");
                }
                if (data.hasExtra("distance")) {
                    maxDistance = data.getIntExtra("distance", 0) * 1000;
                }
                tabLayout.getTabAt(1).select();
                usersFragment.setNewSearchQuery(profession, data.getBooleanExtra("searchByDistance", false), userLocation, maxDistance);
            }
            else if (resultCode == 3) {
                // Open new Request
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //if(!getIntent().getBooleanExtra("employer",true))
            userStatus("online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        userStatus("offline");
    }
}
