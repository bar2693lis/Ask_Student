package com.barlis.chat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    private EditText username_et, email_et, password_et;
    private Button register_btn;

    FirebaseAuth firebaseAuth;
    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Register");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        username_et = findViewById(R.id.username);
        email_et = findViewById(R.id.email);
        password_et = findViewById(R.id.password);

        firebaseAuth = FirebaseAuth.getInstance();

        register_btn = findViewById(R.id.btn_register);
        register_btn.setOnClickListener(new View.OnClickListener() { // Signup button
            @Override
            public void onClick(View v) {
                String txt_username = username_et.getText().toString();
                String txt_email = email_et.getText().toString();
                String txt_password = password_et.getText().toString();

                if(TextUtils.isEmpty(txt_username) || TextUtils.isEmpty(txt_email) || TextUtils.isEmpty(txt_password)){ // Checks if the name or email or password is blank
                    Toast.makeText(RegisterActivity.this, "All fields are required", Toast.LENGTH_SHORT).show();
                }
                else if(txt_password.length() < 6) { // Checks if the password is greater than 6 characters
                    Toast.makeText(RegisterActivity.this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                }
                else{
                    register(txt_username, txt_email, txt_password);
                }
            }
        });
    }

    private void register (final String username, String email, String password){ // Creates a new user with their password and email
        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()) { // If the task was successful
                    FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                    assert firebaseUser != null;
                    String userId = firebaseUser.getUid();

                    databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userId);

                    HashMap<String, String> hashMap = new HashMap<>();
                    hashMap.put("id", userId);
                    hashMap.put("username", username);
                    hashMap.put("imageURL", "default");
                    hashMap.put("email", email);
                    hashMap.put("status", "offline");
                    hashMap.put("search", username.toLowerCase());
                    hashMap.put("facebook", "");
                    hashMap.put("instagram", "");
                    hashMap.put("github", "");
                    hashMap.put("linkedin", "");

                    databaseReference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() { // When all information has been successfully entered
                        @Override
                        public void onComplete(@NonNull Task<Void> task) { // Moves the new user to the main page
                            if(task.isSuccessful()){
                                Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                            }
                        }
                    });
                }
                else{ // When the email or password is incorrect
                    Toast.makeText(RegisterActivity.this, "You can't register with this email or password", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
