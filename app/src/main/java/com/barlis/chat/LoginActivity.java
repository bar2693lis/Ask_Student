package com.barlis.chat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.rengwuxian.materialedittext.MaterialEditText;

public class LoginActivity extends AppCompatActivity {

    private EditText email_et, password_et;
    private Button login_btn;
    private TextView forgot_password_tv;
    private ProgressDialog dialog;
    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Login");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        firebaseAuth = FirebaseAuth.getInstance();

        email_et = findViewById(R.id.email);
        password_et = findViewById(R.id.password);

        forgot_password_tv = findViewById(R.id.forgot_password);
        forgot_password_tv.setOnClickListener(new View.OnClickListener() { // Password reset button
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, ResetPasswordActivity.class));
            }
        });

        login_btn = findViewById(R.id.btn_login);
        login_btn.setOnClickListener(new View.OnClickListener() { // Login button
            @Override
            public void onClick(View v) {
                String txt_email = email_et.getText().toString();
                String txt_password = password_et.getText().toString();

                if(TextUtils.isEmpty(txt_email) || TextUtils.isEmpty(txt_password)) { // Checks if the email or password is empty
                    Toast.makeText(LoginActivity.this, getResources().getString(R.string.missing_fields), Toast.LENGTH_SHORT).show();
                }
                else{ // If the email and password are not empty
                    dialog = new ProgressDialog(LoginActivity.this);
                    dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    dialog.setMessage(getResources().getString(R.string.connecting));
                    dialog.show();
                    firebaseAuth.signInWithEmailAndPassword(txt_email, txt_password).addOnCompleteListener(new OnCompleteListener<AuthResult>() { // Connects with the data email and password
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) { // If the email and password are correct
                            if(task.isSuccessful()){
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                            }
                            else{ // If the email or password is incorrect
                                Toast.makeText(LoginActivity.this, getResources().getString(R.string.authentication_failed), Toast.LENGTH_SHORT).show();
                            }
                            dialog.dismiss();
                        }
                    });
                }
            }
        });
    }
}
