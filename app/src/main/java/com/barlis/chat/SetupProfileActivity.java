package com.barlis.chat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.barlis.chat.Model.User;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class SetupProfileActivity extends AppCompatActivity {

    private FirebaseUser firebaseUser;

    private DatabaseReference databaseReference;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //if(getIntent().getBooleanExtra("employee",true)) {//EMPLOYEE PART
            setContentView(R.layout.setup_profile_layout);
            TextInputEditText profession = findViewById(R.id.ProfileProfessionEt);
            TextInputEditText qualifications = findViewById(R.id.ProfileQualificationEt);
            TextInputEditText experience = findViewById(R.id.ProfileExperienceEt);
            Button nextBtn = findViewById(R.id.ProfileDoneBtn);
            nextBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    {
                        if (profession.getText() != null && qualifications.getText() != null) {
                            firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                            if (firebaseUser != null) {
                                databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
                                HashMap<String, Object> hashMap = new HashMap<>();
                                hashMap.put("profession", profession.getText().toString());
                                hashMap.put("qualifications", qualifications.getText().toString());
                                if(experience.getText()!=null)
                                     hashMap.put("experience", experience.getText().toString());
                                else
                                    hashMap.put("experience","");
                                databaseReference.updateChildren(hashMap);
                                Intent nextWindow = new Intent(SetupProfileActivity.this, SetupPictureActivity.class);
                                startActivity(nextWindow);
                                finish();
                            } else {
                                Toast.makeText(SetupProfileActivity.this, "Error creating database entry,try again later", Toast.LENGTH_SHORT).show();
                            }
                        }
                        else
                        {
                            Toast.makeText(SetupProfileActivity.this,"profession and qualification must be filled",Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });
        //}
       /* else//EMPLOYER PART
        {
            setContentView(R.layout.employer_profile_layout);
            TextInputEditText employerCompanyName = findViewById(R.id.employer_profile_companyNameEt);
            TextInputEditText employerCompanyFocus = findViewById(R.id.employer_profile_companyFocusEt);
            TextInputEditText employerCompanyDescription = findViewById(R.id.employer_profile_companyDescriptionEt);
            Button nextBtn = findViewById(R.id.employerNextBtn);
            nextBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (employerCompanyDescription.getText() != null && employerCompanyFocus.getText() != null && employerCompanyName != null) {
                        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                        if (firebaseUser != null) {
                            databaseReference = FirebaseDatabase.getInstance().getReference("Employers").child(firebaseUser.getUid());
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("companyName", employerCompanyName.getText().toString());
                            hashMap.put("companyFocus", employerCompanyFocus.getText().toString());
                            hashMap.put("companyDescription", employerCompanyDescription.getText().toString());
                            databaseReference.updateChildren(hashMap);
                            Intent nextWindow = new Intent(SetupProfileActivity.this, SetupPictureActivity.class);
                            nextWindow.putExtra("employer",true);
                            startActivity(nextWindow);
                            finish();
                        }
                        else
                        {
                            Toast.makeText(SetupProfileActivity.this,"Error creating database entry,try again later",Toast.LENGTH_SHORT).show();
                        }

                    }
                    else
                    {
                        Toast.makeText(SetupProfileActivity.this, "You must fill all the Information in order to continue", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }*/
    }
}
