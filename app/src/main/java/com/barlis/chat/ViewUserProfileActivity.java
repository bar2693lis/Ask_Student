package com.barlis.chat;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedDispatcherOwner;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.barlis.chat.Fragments.RateUserDialogFragment;
import com.barlis.chat.Model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ViewUserProfileActivity extends AppCompatActivity implements RateUserDialogFragment.MyDialogListener {

    final String RATE_FRAGMENT_TAG = "RATE_FRAGMENT_TAG";
    CircleImageView image_profile;
    TextView username, email, reviewerCount;
    ImageView facebook_account, instagram_account, github_account, linkedin_account;
    ImageView stars[];
    LinearLayout starLayout;

    private String facebookUrl, instagramUrl, githubUrl, linkedinUrl, userId;
    private User user;

    DatabaseReference mDatabaseReference;
    DatabaseReference mDatabaseUserRatingReference;
    FirebaseUser firebaseUser;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_user_profile);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;
        int height = displayMetrics.heightPixels;
        getWindow().setLayout((int)(width*0.8), (int)(height*0.55));
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.gravity = Gravity.CENTER;
        getWindow().setAttributes(params);

        image_profile = findViewById(R.id.profile_image);
        username = findViewById(R.id.username);
        email = findViewById(R.id.email);
        facebook_account = findViewById(R.id.facebook_account);
        instagram_account = findViewById(R.id.instagram_account);
        github_account = findViewById(R.id.github_account);
        linkedin_account = findViewById(R.id.linkedin_account);
        starLayout = findViewById(R.id.star_layout);
        stars = new ImageView[5];
        stars[0] = findViewById(R.id.star_one);
        stars[1] = findViewById(R.id.star_two);
        stars[2] = findViewById(R.id.star_three);
        stars[3] = findViewById(R.id.star_four);
        stars[4] = findViewById(R.id.star_five);
        reviewerCount = findViewById(R.id.reviewer_count_text);

        userId = getIntent().getStringExtra("userId");
        mDatabaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userId);

        mDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                user = dataSnapshot.getValue(User.class);
                username.setText(user.getUsername());
                email.setText(user.getEmail());
                facebookUrl = user.getFacebook();
                instagramUrl = user.getInstagram();
                githubUrl = user.getGithub();
                linkedinUrl = user.getLinkedin();

                if(user.getImageURL().equals("default")){
                    image_profile.setImageResource(R.mipmap.ic_launcher);
                }
                else{
                    Picasso.get().load(user.getImageURL()).into(image_profile);
                }

                reviewerCount.setText(user.getNumberOfReviews() + " " + getResources().getString(R.string.reviews));
                fillStarRating(user.getRating());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        facebook_account.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!facebookUrl.equals("")){
                    openWebsite(facebookUrl);
                }
                else {
                    Toast.makeText(ViewUserProfileActivity.this, "No Facebook account found", Toast.LENGTH_SHORT).show();
                }
            }
        });

        instagram_account.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!instagramUrl.equals("")){
                    openWebsite(instagramUrl);
                }
                else {
                    Toast.makeText(ViewUserProfileActivity.this, "No Instagram account found", Toast.LENGTH_SHORT).show();
                }
            }
        });

        github_account.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!githubUrl.equals("")){
                    openWebsite(githubUrl);
                }
                else {
                    Toast.makeText(ViewUserProfileActivity.this, "No GitHub account found", Toast.LENGTH_SHORT).show();
                }
            }
        });

        linkedin_account.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!linkedinUrl.equals("")){
                    openWebsite(linkedinUrl);
                }
                else {
                    Toast.makeText(ViewUserProfileActivity.this, "No Linkedin account found", Toast.LENGTH_SHORT).show();
                }
            }
        });

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();


        starLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDatabaseUserRatingReference = FirebaseDatabase.getInstance().getReference("RatingHistory").child(userId).child(firebaseUser.getUid());
                mDatabaseUserRatingReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        long previousRating;
                        if (dataSnapshot.child("rating").getValue() == null) {
                            previousRating = 0;
                        }
                        else {
                            previousRating = (long)dataSnapshot.child("rating").getValue();
                        }
                        RateUserDialogFragment rateUserDialogFragment = new RateUserDialogFragment(user.getUsername(), user.getImageURL(), (int)previousRating);
                        rateUserDialogFragment.show(getSupportFragmentManager(), RATE_FRAGMENT_TAG);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });
    }

    private void openWebsite(String url) {
        Uri uri = Uri.parse(url);
        Intent launchWebsite = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(launchWebsite);
    }

    private void fillStarRating(float rating) {
        for (int i = 0; i < 5; i++) {
            stars[i].setImageResource(R.drawable.empty_star);
        }

        int currentStar = 0;
        while (rating >= 1) {
            rating--;
            stars[currentStar].setImageResource(R.drawable.full_star);
            currentStar++;
        }
        if (rating != 0) {
            stars[currentStar].setImageResource(R.drawable.half_star_bitmap);
        }
    }

    // Update user rating
    @Override
    public void onReturn(int rating, int oldRating) {
        float newRating;
        HashMap<String, Object> hashMap = new HashMap<>();
        // If user never rated
        if (oldRating == 0) {
            newRating = ((user.getRating() * user.getNumberOfReviews()) + rating) / (user.getNumberOfReviews() + 1);
            hashMap.put("numberOfReviews", user.getNumberOfReviews() + 1);
        }
        // Update only if user changed his rating
        else if (oldRating != rating) {
            newRating = ((user.getRating() * user.getNumberOfReviews()) - (oldRating - rating)) / user.getNumberOfReviews();
        }
        else {
            return;
        }
        HashMap<String, Object> ratingHistory = new HashMap<>();
        ratingHistory.put("rating", rating);
        mDatabaseUserRatingReference.updateChildren(ratingHistory);
        hashMap.put("rating", newRating);
        mDatabaseReference.updateChildren(hashMap);
    }
}
