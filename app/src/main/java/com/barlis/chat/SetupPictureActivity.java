package com.barlis.chat;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupPictureActivity extends AppCompatActivity {
    private int PICK_PICTURE = 100;
    private CircleImageView circleImageView;
    private Button skipBtn;
    private Uri imageUri = null;
    private StorageTask<UploadTask.TaskSnapshot> uploadTask;
    private DatabaseReference databaseReference;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setup_picture_layout);
        circleImageView = findViewById(R.id.setup_Picture_image);
        Button TakePicBtn = findViewById(R.id.setup_Picture_takePicBtn);
        skipBtn = findViewById(R.id.setup_Picture_skipBtn);
        skipBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SetupPictureActivity.this,MainActivity.class);
                /*if(getIntent().hasExtra("employer"))
                {
                    intent.putExtra("employer",true);
                    System.out.println("setup Picture employer true");
                }
                else
                {
                    intent.putExtra("employer",false);
                    System.out.println("setup Picture employer false");
                }*/
                /*intent.putExtra("userProfession",getIntent().getStringExtra("userProfession"));
                intent.putExtra("userQualification",getIntent().getStringExtra("userQualification"));
                intent.putExtra("userExperience",getIntent().getStringExtra("userExperience"));
                intent.putExtra("personal",getIntent().getStringExtra("personal"));
                if(imageUri!=null)
                    intent.putExtra("imageUri",imageUri);*/
                startActivity(intent);
                finish();
            }
        });
        TakePicBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent pickPhoto = new Intent(Intent.ACTION_PICK);
                pickPhoto.setType("image/*");
                startActivityForResult(pickPhoto,PICK_PICTURE);
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK)
        {
            try{
                imageUri = data.getData();
                InputStream imageStream = getContentResolver().openInputStream(imageUri);
                Bitmap selectedPicture = BitmapFactory.decodeStream(imageStream);
                circleImageView.setImageBitmap(selectedPicture);
                skipBtn.setText("NEXT");
                FirebaseImageUpload();
            }catch (FileNotFoundException e){
                e.printStackTrace();
                Toast.makeText(SetupPictureActivity.this,"an error happened while selecting a picture",Toast.LENGTH_SHORT).show();
            }
        }
        else
        {
            Toast.makeText(SetupPictureActivity.this,"permission Required",Toast.LENGTH_SHORT).show();
        }
    }

    private void FirebaseImageUpload() {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference("Uploads");
        StorageReference pictureReference = storageReference.child(System.currentTimeMillis() + "." + "");
        uploadTask = pictureReference.putFile(imageUri);
        uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw Objects.requireNonNull(task.getException());
                }
                return pictureReference.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if(task.isSuccessful())
                {
                    Uri dUri = task.getResult();
                    if(dUri!=null) {
                        String sUri = dUri.toString();
                        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                        if(firebaseUser!=null) {
                           /* if(getIntent().hasExtra("employer"))
                                databaseReference = FirebaseDatabase.getInstance().getReference("Employers").child(firebaseUser.getUid());
                            else*/
                                databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
                            HashMap<String,Object>hashMap = new HashMap<>();
                            hashMap.put("imageURL",sUri);
                            databaseReference.updateChildren(hashMap);
                        }
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(SetupPictureActivity.this,"failed setting picture",Toast.LENGTH_SHORT).show();
            }
        });
    }
}
