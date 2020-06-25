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
    boolean gotUserLocation = false;
    int maxDistance;
    TabLayout tabLayout;
    FirebaseUser firebaseUser;
    DatabaseReference reference;
    private UsersFragment usersFragment;
    private RequestsFragment requestsFragment;
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
                        viewPagerAdapter.addFragment(new ChatsFragment(), getResources().getString(R.string.chats_tab));
                    } else {
                        viewPagerAdapter.addFragment(new ChatsFragment(), "(" + unreadMessages + ") " + getResources().getString(R.string.chats_tab));
                    }

                    //viewPagerAdapter.addFragment(new UsersFragment(), "Users");
                    viewPagerAdapter.addFragment(usersFragment, getResources().getString(R.string.users_tab));
                    viewPagerAdapter.addFragment(requestsFragment, getResources().getString(R.string.requests_tab));
                    viewPagerAdapter.addFragment(new ProfileFragment(), getResources().getString(R.string.profile_tab));

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
                    reference.updateChildren(map);
                    gotUserLocation = true;
                }
                // Only update db when user traveled more than 5 meters
                if (userLocation.distanceTo(location) > 5) {
                    userLocation.setLatitude(location.getLatitude());
                    userLocation.setLongitude(location.getLongitude());
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("latitude", location.getLatitude());
                    map.put("longitude", location.getLongitude());
                    reference.updateChildren(map);
                }
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
            case R.id.my_requests:
                startActivity(new Intent(MainActivity.this, UserSpecificRequestsActivity.class).putExtra("user_name", username.getText().toString()));
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
        if (requestCode == ERequestCodes.NEW_REQUEST.getValue()) {
            if (resultCode == EResultCodes.SEARCH_USER.getValue()) {
                if (data.hasExtra("profession")) {
                    profession = data.getStringExtra("profession");
                }
                if (data.hasExtra("distance")) {
                    maxDistance = data.getIntExtra("distance", 0) * 1000;
                }
                tabLayout.getTabAt(1).select();
                usersFragment.setNewSearchQuery(profession, data.getBooleanExtra("searchByDistance", false), userLocation, maxDistance);
            }
            else if (resultCode == EResultCodes.OPEN_REQUEST.getValue()) {
                // Open new Request
                Request request = (Request) data.getSerializableExtra("request");
                request.setCreatorId(firebaseUser.getUid());
                request.setCreatorName(username.getText().toString());
                requestsFragment.addRequest(request);
            }
        }
        else if (requestCode == ERequestCodes.UPDATE_REQUEST.getValue()) {
            if (resultCode == EResultCodes.UPDATE_REQUEST_WORKER.getValue()) {
                requestsFragment.updateRequestWorker(data.getIntExtra("request_position", 0), username.getText().toString(), firebaseUser.getUid());
                Intent intent = new Intent(MainActivity.this, MessageActivity.class);
                intent.putExtra("userId", data.getStringExtra("creatorId"));
                startActivity(intent);
                sendNotification(data.getStringExtra("creatorId"), getResources().getString(R.string.job_taken_message_body), username.getText().toString() + " " + getResources().getString(R.string.job_taken_notification));
            }
            else if (resultCode == EResultCodes.REMOVE_WORKER.getValue()) {
                requestsFragment.removeWorkerFromRequest(data.getIntExtra("request_position", 0), data.getStringExtra("workerId"));
                sendNotification(data.getStringExtra("workerId"), username.getText().toString() + " " + getResources().getString(R.string.removed_from_request_alert), getResources().getString(R.string.request_update_alert));
            }
            else if (resultCode == EResultCodes.QUIT_REQUEST.getValue()) {
                requestsFragment.removeWorkerFromRequest(data.getIntExtra("request_position", 0), firebaseUser.getUid());
                sendNotification(data.getStringExtra("creatorId"), username.getText().toString() + " " + getResources().getString(R.string.worker_quit_alert), getResources().getString(R.string.request_update_alert));
            }
            else if (resultCode == EResultCodes.CLOSE_REQUEST.getValue()) {
                requestsFragment.closeRequest(data.getIntExtra("request_position",0));
                sendNotification(data.getStringExtra("workerId"), username.getText().toString() + " " + getResources().getString(R.string.request_closed_alert), getResources().getString(R.string.request_update_alert));
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

    private void sendNotification(String receiver, String body, String title){
        DatabaseReference token = FirebaseDatabase.getInstance().getReference("Tokens");

        Query query = token.orderByKey().equalTo(receiver);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Token token = snapshot.getValue(Token.class);

                    Data data = new Data(firebaseUser.getUid(), body, title, receiver);

                    Sender sender = new Sender(data, token.getToken());
                    APIService apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);
                    apiService.sendNotification(sender).enqueue(new Callback<MyResponse>() {
                        @Override
                        public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                            if(response.code() == 200){
                                if(response.body().success != 1){
                                    Toast.makeText(MainActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<MyResponse> call, Throwable t) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
