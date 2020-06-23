package com.barlis.chat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.barlis.chat.Fragments.ChatsFragment;
import com.barlis.chat.Fragments.GroupChatFragment;
import com.barlis.chat.Fragments.ProfileFragment;
import com.barlis.chat.Fragments.UsersFragment;
import com.barlis.chat.Model.Chat;
import com.barlis.chat.Model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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

import java.util.ArrayList;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    CircleImageView profileImage;
    TextView username;

    //Start Hanan part
    CoordinatorLayout coordinatorLayout;
    FloatingActionButton floatingBtn;
    String profession = "",job_description = "",note = "",qualifications = "";//looking for
    //End Hanan part
    FirebaseUser firebaseUser;
    DatabaseReference reference;
    FirebaseDatabase rootReference;
   // String userProfession,userQualification,userExperience,userPersonal;
    private Uri imageUri = null;
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
                    startActivity(popUpIntent);
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

            rootReference = FirebaseDatabase.getInstance();

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
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            TabLayout tabLayout = findViewById(R.id.tab_layout);
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

                    viewPagerAdapter.addFragment(new GroupChatFragment(), "Groups"); // Start, Bar
                    CreateNewGroup(); // End, Bar


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

    private void CreateNewGroup() { // Start, Bar
        rootReference.getReference().child("Groups").child("Computer Science - Work Group").setValue("").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){

                }
            }
        });
        rootReference.getReference().child("Groups").child("Electronic Engineering - Work Group").setValue("").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){

                }
            }
        });
        rootReference.getReference().child("Groups").child("Applied Mathematics - Work Group").setValue("").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){

                }
            }
        });
        rootReference.getReference().child("Groups").child("Industrial design - Work Group").setValue("").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){

                }
            }
        });
    } // End, Bar

    private void userStatus(String status){
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status", status);

        reference.updateChildren(hashMap);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //if(!getIntent().getBooleanExtra("employer",true))
            userStatus("online");
        //getting information back from popUp activity
        if(getIntent().hasExtra("Profession")) {
            profession = getIntent().getStringExtra("Profession");
        }
        if(getIntent().hasExtra("Job Description")) {
            job_description = getIntent().getStringExtra("Job Description");
        }
        if(getIntent().hasExtra("note")) {
            note = getIntent().getStringExtra("note");
        }
        if(getIntent().hasExtra("qualifications")){
            qualifications = getIntent().getStringExtra("qualifications");
        }
        //call for fragment update
        if(getIntent().hasExtra("distance"))
            usersFragment.setNewSearchQuery(profession,qualifications);

        //opens job ticket page

    }

    @Override
    protected void onPause() {
        super.onPause();
        userStatus("offline");
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
}
