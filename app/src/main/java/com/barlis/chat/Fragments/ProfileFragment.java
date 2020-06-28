package com.barlis.chat.Fragments;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.barlis.chat.Model.User;
import com.barlis.chat.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;

public class ProfileFragment extends Fragment {

    private CircleImageView image_profile_civ, cancel_btn, save_btn;
    private TextView username_tv, email_tv, reviewerCount, profession_tv;
    private ImageView choose_image_btn, edit_accounts_btn, facebook_account_btn, instagram_account_btn, github_account_btn, linkedin_account_btn, stars[];
    private EditText edit_facebook_account, edit_instagram_account, edit_github_account, edit_linkedin_account, edit_profession, edit_qualifications ,edit_experience, edit_personal;
    private LinearLayout accounts, edit_accounts, edit_details;
    private String facebook_url, instagram_url, github_url, linkedin_url;
    private static final int IMAGE_REQUEST = 1;
    private Uri image_uri;

    private StorageTask<UploadTask.TaskSnapshot> uploadTask;

    DatabaseReference databaseReference;
    FirebaseUser firebaseUser;
    StorageReference storageReference;

    public ProfileFragment() { // Default constructor
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        image_profile_civ = view.findViewById(R.id.profile_image);
        username_tv = view.findViewById(R.id.username);
        profession_tv = view.findViewById(R.id.profession);
        email_tv = view.findViewById(R.id.email);

        accounts = view.findViewById(R.id.accounts);
        edit_accounts = view.findViewById(R.id.edit_accounts);
        edit_details = view.findViewById(R.id.edit_details);

        edit_facebook_account = view.findViewById(R.id.edit_facebook_account);
        edit_instagram_account = view.findViewById(R.id.edit_instagram_account);
        edit_github_account = view.findViewById(R.id.edit_github_account);
        edit_linkedin_account = view.findViewById(R.id.edit_linkedin_account);

        edit_profession = view.findViewById(R.id.edit_profession);
        edit_qualifications = view.findViewById(R.id.edit_qualifications);
        edit_experience = view.findViewById(R.id.edit_experience);
        edit_personal = view.findViewById(R.id.edit_personal);

        stars = new ImageView[5];
        stars[0] = view.findViewById(R.id.star_one);
        stars[1] = view.findViewById(R.id.star_two);
        stars[2] = view.findViewById(R.id.star_three);
        stars[3] = view.findViewById(R.id.star_four);
        stars[4] = view.findViewById(R.id.star_five);

        reviewerCount = view.findViewById(R.id.reviewer_count_text);

        storageReference = FirebaseStorage.getInstance().getReference("Uploads");

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        databaseReference.addValueEventListener(new ValueEventListener() { // Retrieves user information
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                username_tv.setText(user.getUsername());
                profession_tv.setText(user.getProfession());
                email_tv.setText(user.getEmail());
                facebook_url = user.getFacebook();
                instagram_url = user.getInstagram();
                github_url = user.getGithub();
                linkedin_url = user.getLinkedin();

                if(user.getImageURL().equals("default")){  // When there is no link to the image use the default
                    image_profile_civ.setImageResource(R.mipmap.ic_launcher);
                }
                else{
                    Picasso.get().load(user.getImageURL()).into(image_profile_civ);
                }

                reviewerCount.setText(user.getNumberOfReviews() + " " + getResources().getString(R.string.reviews));
                fillStarRating(user.getRating());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        choose_image_btn = view.findViewById(R.id.choose_image);
        choose_image_btn.setOnClickListener(new View.OnClickListener() { // Picture Selection Button
            @Override
            public void onClick(View v) {
                openImage();
            }
        });

        facebook_account_btn = view.findViewById(R.id.facebook_account);
        facebook_account_btn.setOnClickListener(new View.OnClickListener() { // Facebook account button
            @Override
            public void onClick(View v) {
                if(!facebook_url.equals("")){
                    openWebsite(facebook_url);
                }
                else { // If there is no account then it displays a message
                    Toast.makeText(getContext(), getResources().getString(R.string.no_facebook_account_found), Toast.LENGTH_SHORT).show();
                }
            }
        });

        instagram_account_btn = view.findViewById(R.id.instagram_account);
        instagram_account_btn.setOnClickListener(new View.OnClickListener() { // Instagram account button
            @Override
            public void onClick(View v) {
                if(!instagram_url.equals("")){
                    openWebsite(instagram_url);
                }
                else { // If there is no account then it displays a message
                    Toast.makeText(getContext(), getResources().getString(R.string.no_instagram_account_found), Toast.LENGTH_SHORT).show();
                }
            }
        });

        github_account_btn = view.findViewById(R.id.github_account);
        github_account_btn.setOnClickListener(new View.OnClickListener() { // GitHub account button
            @Override
            public void onClick(View v) {
                if(!github_url.equals("")){
                    openWebsite(github_url);
                }
                else { // If there is no account then it displays a message
                    Toast.makeText(getContext(), getResources().getString(R.string.no_github_account_found), Toast.LENGTH_SHORT).show();
                }
            }
        });

        linkedin_account_btn = view.findViewById(R.id.linkedin_account);
        linkedin_account_btn.setOnClickListener(new View.OnClickListener() { // Linkedin account button
            @Override
            public void onClick(View v) {
                if(!linkedin_url.equals("")){
                    openWebsite(linkedin_url);
                }
                else { // If there is no account then it displays a message
                    Toast.makeText(getContext(), getResources().getString(R.string.no_linkedin_account_found), Toast.LENGTH_SHORT).show();
                }
            }
        });

        LinearLayout edit_accounts_layout = view.findViewById(R.id.edit_accounts_layout);
        edit_accounts_layout.setOnClickListener(new View.OnClickListener() { // Edit button for all accounts
            @Override
            public void onClick(View v) {
                accounts.setVisibility(View.GONE);
                edit_accounts.setVisibility(View.VISIBLE); // Account edit lines appear
            }
        });
        LinearLayout edit_details_layout = view.findViewById(R.id.edit_details_layout);
        edit_details_layout.setOnClickListener(new View.OnClickListener() { // Edit button for all details
            @Override
            public void onClick(View v) {
                accounts.setVisibility(View.GONE);
                edit_details.setVisibility(View.VISIBLE); // Details edit lines appear
            }
        });

        save_btn = view.findViewById(R.id.save_btn);
        save_btn.setOnClickListener(new View.OnClickListener() { // Accounts Save Button
            @Override
            public void onClick(View v) {
                databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

                if(edit_facebook_account.getText().toString().equals("") && edit_instagram_account.getText().toString().equals("") && edit_github_account.getText().toString().equals("") && edit_linkedin_account.getText().toString().equals("")){ // When no account has been added and the user wants to save
                    Toast.makeText(getContext(), getResources().getString(R.string.no_accounts_added), Toast.LENGTH_SHORT).show();
                }
                else { // When one of the accounts was updated
                    HashMap<String, Object> hashMap = new HashMap<>();

                    if(!edit_facebook_account.getText().toString().equals("")){
                        hashMap.put("facebook", "https://www.facebook.com/" + edit_facebook_account.getText().toString() + "/");
                        edit_facebook_account.setText("");
                    }
                    if(!edit_instagram_account.getText().toString().equals("")){
                        hashMap.put("instagram", "https://www.instagram.com/" + edit_instagram_account.getText().toString() + "/");
                        edit_instagram_account.setText("");
                    }
                    if(!edit_github_account.getText().toString().equals("")){
                        hashMap.put("github", "https://github.com/" + edit_github_account.getText().toString() + "/");
                        edit_github_account.setText("");
                    }
                    if(!edit_linkedin_account.getText().toString().equals("")){
                        hashMap.put("linkedin", "https://www.linkedin.com/in/" + edit_linkedin_account.getText().toString() + "/");
                        edit_linkedin_account.setText("");
                    }

                    databaseReference.updateChildren(hashMap);

                    accounts.setVisibility(View.VISIBLE);
                    edit_accounts.setVisibility(View.GONE);

                    Toast.makeText(getContext(), getResources().getString(R.string.accounts_saved), Toast.LENGTH_SHORT).show();
                }
            }
        });

        cancel_btn = view.findViewById(R.id.cancel_btn);
        cancel_btn.setOnClickListener(new View.OnClickListener() { // Accounts Cancel Button
            @Override
            public void onClick(View v) {
                accounts.setVisibility(View.VISIBLE);
                edit_accounts.setVisibility(View.GONE);
            }
        });

        ImageView save_details_btn = view.findViewById(R.id.save_details_btn);
        save_details_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

                if(edit_profession.getText().toString().equals("") && edit_qualifications.getText().toString().equals("") && edit_experience.getText().toString().equals("") && edit_personal.getText().toString().equals("")){ // When no details has been added and the user wants to save
                    Toast.makeText(getContext(), getResources().getString(R.string.no_new_details), Toast.LENGTH_SHORT).show();
                }
                else { // When at least one detail was updated
                    HashMap<String, Object> hashMap = new HashMap<>();

                    if(!edit_profession.getText().toString().equals("")){
                        hashMap.put("profession", edit_profession.getText().toString());
                        edit_profession.setText("");
                    }
                    if(!edit_qualifications.getText().toString().equals("")){
                        hashMap.put("qualifications", edit_qualifications.getText().toString());
                        edit_qualifications.setText("");
                    }
                    if(!edit_experience.getText().toString().equals("")){
                        hashMap.put("experience", edit_experience.getText().toString());
                        edit_experience.setText("");
                    }
                    if(!edit_personal.getText().toString().equals("")){
                        hashMap.put("personal", edit_personal.getText().toString());
                        edit_personal.setText("");
                    }

                    databaseReference.updateChildren(hashMap);

                    accounts.setVisibility(View.VISIBLE);
                    edit_details_layout.setVisibility(View.GONE);

                    Toast.makeText(getContext(), getResources().getString(R.string.accounts_saved), Toast.LENGTH_SHORT).show();
                }
            }
        });

        ImageView cancel_details_btn = view.findViewById(R.id.cancel_details_btn);
        cancel_details_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                accounts.setVisibility(View.VISIBLE);
                edit_details.setVisibility(View.GONE);
            }
        });

        return view;
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

    private void openWebsite(String url) { // When one of the accounts buttons is clicked, the website opens
        Uri uri = Uri.parse(url);
        Intent launchWebsite = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(launchWebsite);
    }

    private void openImage() { // When you click the Picture Selection button, the Pictures window opens
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, IMAGE_REQUEST);
    }

    private String getFileExtension(Uri uri){
        ContentResolver contentResolver = getContext().getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void uploadImage(){ // When the user uploads a new image
        final ProgressDialog pd = new ProgressDialog(getContext());
        pd.setMessage("Uploading");
        pd.show();

        if(image_uri != null){ // If the uri is not empty
            final StorageReference fileReference = storageReference.child(System.currentTimeMillis() + "." + getFileExtension(image_uri)); // Image name contains current time and extension

            uploadTask = fileReference.putFile(image_uri); // To upload a file to Cloud Storage, you first create a reference to the full path of the file, including the file name
            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() { // Returns a new Task that will be completed with the result of applying the specified Continuation to this Task (The Continuation will be called on the main application thread)
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if(!task.isSuccessful()){ // If the task fails
                        throw Objects.requireNonNull(task.getException());
                    }

                    return fileReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() { // To handle success and failure in the same listener
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if(task.isSuccessful()){ // If the task was successful
                        Uri downloadUri = task.getResult();
                        assert downloadUri != null;
                        String strUri = downloadUri.toString();

                        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
                        HashMap<String, Object> map = new HashMap<>();
                        map.put("imageURL", strUri);
                        databaseReference.updateChildren(map);

                        pd.dismiss();
                    }
                    else{ // If image upload failed
                        Toast.makeText(getContext(), getResources().getString(R.string.upload_failed), Toast.LENGTH_SHORT).show();
                        pd.dismiss();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() { // To be notified when the task fails
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    pd.dismiss();
                }
            });
        }
        else{ // If no image is selected
            Toast.makeText(getContext(), getResources().getString(R.string.no_image_selected), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) { // Start another activity and receive a result back
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null){ // When all requests are valid and the information is not empty
            image_uri = data.getData();

            if(uploadTask != null && uploadTask.isInProgress()){ // When the image is still uploading
                Toast.makeText(getContext(), getResources().getString(R.string.upload_in_progress), Toast.LENGTH_SHORT).show();
            }
            else{
                uploadImage();
            }
        }
    }
}