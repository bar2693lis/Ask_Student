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
import android.widget.Button;
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

    CircleImageView image_profile, cancel_btn, save_btn;
    TextView username, email;
    ImageView choose_image, edit_accounts_btn, facebook_account, instagram_account, github_account, linkedin_account;
    EditText edit_facebook_account, edit_instagram_account, edit_github_account, edit_linkedin_account;
    LinearLayout accounts, edit_accounts;

    private String facebookUrl, instagramUrl, githubUrl, linkedinUrl;

    DatabaseReference mDatabaseReference;
    FirebaseUser mFirebaseUser;
    StorageReference mStorageReference;

    private static final int IMAGE_REQUEST = 1;

    private Uri imageURI;

    private StorageTask<UploadTask.TaskSnapshot> uploadTask;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        image_profile = view.findViewById(R.id.profile_image);
        username = view.findViewById(R.id.username);
        email = view.findViewById(R.id.email);
        facebook_account = view.findViewById(R.id.facebook_account);
        instagram_account = view.findViewById(R.id.instagram_account);
        github_account = view.findViewById(R.id.github_account);
        linkedin_account = view.findViewById(R.id.linkedin_account);

        choose_image = view.findViewById(R.id.choose_image);
        edit_accounts_btn = view.findViewById(R.id.edit_accounts_btn);

        accounts = view.findViewById(R.id.accounts);
        edit_accounts = view.findViewById(R.id.edit_accounts);

        edit_facebook_account = view.findViewById(R.id.edit_facebook_account);
        edit_instagram_account = view.findViewById(R.id.edit_instagram_account);
        edit_github_account = view.findViewById(R.id.edit_github_account);
        edit_linkedin_account = view.findViewById(R.id.edit_linkedin_account);

        save_btn = view.findViewById(R.id.save_btn);
        cancel_btn = view.findViewById(R.id.cancel_btn);

        mStorageReference = FirebaseStorage.getInstance().getReference("Uploads");

        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference("Users").child(mFirebaseUser.getUid());

        mDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
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
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        choose_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImage();
            }
        });

        facebook_account.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!facebookUrl.equals("")){
                    openWebsite(facebookUrl);
                }
                else {
                    Toast.makeText(getContext(), "No Facebook account found", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(getContext(), "No Instagram account found", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(getContext(), "No GitHub account found", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(getContext(), "No Linkedin account found", Toast.LENGTH_SHORT).show();
                }
            }
        });

        edit_accounts_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                accounts.setVisibility(View.GONE);
                edit_accounts.setVisibility(View.VISIBLE);
            }
        });

        save_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDatabaseReference = FirebaseDatabase.getInstance().getReference("Users").child(mFirebaseUser.getUid());

                if(edit_facebook_account.getText().toString().equals("") && edit_instagram_account.getText().toString().equals("") && edit_github_account.getText().toString().equals("") && edit_linkedin_account.getText().toString().equals("")){
                    Toast.makeText(getContext(), "You haven't added any accounts", Toast.LENGTH_SHORT).show();
                }
                else {
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

                    mDatabaseReference.updateChildren(hashMap);

                    accounts.setVisibility(View.VISIBLE);
                    edit_accounts.setVisibility(View.GONE);

                    Toast.makeText(getContext(), "Accounts saved", Toast.LENGTH_SHORT).show();
                }
            }
        });

        cancel_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                accounts.setVisibility(View.VISIBLE);
                edit_accounts.setVisibility(View.GONE);

                Toast.makeText(getContext(), "Accounts didn't saved", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    private void openWebsite(String url) {
        Uri uri = Uri.parse(url);
        Intent launchWebsite = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(launchWebsite);
    }

    private void openImage() {
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

    private void uploadImage(){
        final ProgressDialog pd = new ProgressDialog(getContext());
        pd.setMessage("Uploading");
        pd.show();

        if(imageURI != null){
            final StorageReference fileReference = mStorageReference.child(System.currentTimeMillis() + "." + getFileExtension(imageURI));

            uploadTask = fileReference.putFile(imageURI);
            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if(!task.isSuccessful()){
                        throw Objects.requireNonNull(task.getException());
                    }

                    return fileReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if(task.isSuccessful()){
                        Uri downloadUri = task.getResult();
                        assert downloadUri != null;
                        String mUri = downloadUri.toString();

                        mDatabaseReference = FirebaseDatabase.getInstance().getReference("Users").child(mFirebaseUser.getUid());
                        HashMap<String, Object> map = new HashMap<>();
                        map.put("imageURL", mUri);
                        mDatabaseReference.updateChildren(map);

                        pd.dismiss();
                    }
                    else{
                        Toast.makeText(getContext(), "Failed!", Toast.LENGTH_SHORT).show();
                        pd.dismiss();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    pd.dismiss();
                }
            });
        }
        else{
            Toast.makeText(getContext(), "No image selected", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null){
            imageURI = data.getData();

            if(uploadTask != null && uploadTask.isInProgress()){
                Toast.makeText(getContext(), "Upload in progress", Toast.LENGTH_SHORT).show();
            }
            else{
                uploadImage();
            }
        }
    }
}