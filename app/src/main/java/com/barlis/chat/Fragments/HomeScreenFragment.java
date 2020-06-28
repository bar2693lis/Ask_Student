package com.barlis.chat.Fragments;

import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.barlis.chat.MessageActivity;
import com.barlis.chat.Model.Chat;
import com.barlis.chat.Model.ERequestCodes;
import com.barlis.chat.Model.Request;
import com.barlis.chat.Notification.NotificationBuilder;
import com.barlis.chat.PopUpActivity;
import com.barlis.chat.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class HomeScreenFragment extends Fragment {
    private TabLayout tabLayout;
    private UsersFragment usersFragment;
    private RequestsFragment requestsFragment;
    private DatabaseReference databaseReference;
    private FirebaseUser firebaseUser;
    private Location userLocation;
    private String userName;

    public HomeScreenFragment() {
    }

    public void setFirebaseUser(FirebaseUser firebaseUser) {
        this.firebaseUser = firebaseUser;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setUserLocation(Location userLocation) {
        this.userLocation = userLocation;
    }

    //Start Hanan part
    CoordinatorLayout coordinatorLayout;
    FloatingActionButton floatingBtn;
    String profession = "",job_description = "",note = "",qualifications = "";//looking for
    private Uri imageUri = null;
    //End Hanan part

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_screen, container, false);

        usersFragment = new UsersFragment();
        requestsFragment = new RequestsFragment();

        floatingBtn = view.findViewById(R.id.floatingBtn);
        floatingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent popUpIntent = new Intent(getContext(), PopUpActivity.class);
                startActivityForResult(popUpIntent, ERequestCodes.NEW_REQUEST.getValue());
            }
        });

        tabLayout = view.findViewById(R.id.tab_layout);

        ViewPager viewPager = view.findViewById(R.id.view_pager);

        databaseReference = FirebaseDatabase.getInstance().getReference("Chats");
        databaseReference.addValueEventListener(new ValueEventListener() { // reference to the chats of the current user
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                HomeScreenFragment.ViewPagerAdapter viewPagerAdapter = new HomeScreenFragment.ViewPagerAdapter(getActivity().getSupportFragmentManager());

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

        return view;
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {

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


    public void searchUsers(String profession, boolean searchByDistance, int maxDistance) {
        // Search for users and move to the users tab
        tabLayout.getTabAt(1).select();
        usersFragment.setNewSearchQuery(profession, searchByDistance, userLocation, maxDistance);
    }

    public void openRequest(Request request) {
        // Open new Request
        request.setCreatorId(firebaseUser.getUid());
        request.setCreatorName(userName);
        requestsFragment.addRequest(request);
    }

    public void addWorkerToRequest(int position, String creatorId) {
        // Add worker to request and send that user a notification
        requestsFragment.updateRequestWorker(position, userName, firebaseUser.getUid());
        Intent intent = new Intent(getContext(), MessageActivity.class);
        intent.putExtra("userId", creatorId);
        startActivity(intent);
        NotificationBuilder.sendNotification(getContext(), firebaseUser.getUid(), creatorId, getResources().getString(R.string.job_taken_message_body), userName + " " + getResources().getString(R.string.job_taken_notification));

    }

    public void removeWorkerFromRequest(int position, String workerId) {
        // Remove worker from request and send that user a notification
        requestsFragment.removeWorkerFromRequest(position, workerId);
        NotificationBuilder.sendNotification(getContext(), firebaseUser.getUid(), workerId, userName + " " + getResources().getString(R.string.removed_from_request_alert), getResources().getString(R.string.request_update_alert));
    }

    public void closeRequest(int position, String workerId) {
        // Close request and send the worker a notification
        requestsFragment.closeRequest(position);
        NotificationBuilder.sendNotification(getContext(), firebaseUser.getUid(), workerId, userName + " " + getResources().getString(R.string.request_closed_alert), getResources().getString(R.string.request_update_alert));
    }

    public void quitFromRequest(int position, String creatorId) {
        requestsFragment.removeWorkerFromRequest(position, firebaseUser.getUid());
        NotificationBuilder.sendNotification(getContext(), firebaseUser.getUid(), creatorId, userName + " " + getResources().getString(R.string.worker_quit_alert), getResources().getString(R.string.request_update_alert));

    }
}
