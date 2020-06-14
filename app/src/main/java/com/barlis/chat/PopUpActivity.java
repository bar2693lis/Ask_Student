package com.barlis.chat;

import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.barlis.chat.Model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class PopUpActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.popup_layout);
        EditText professionEt = findViewById(R.id.professionEt);
        EditText jobDesc = findViewById(R.id.jobDesc);
        EditText qualifications = findViewById(R.id.qualificationsEt);
        EditText note = findViewById(R.id.note);
        Button doneBtn = findViewById(R.id.doneBtn);
        EditText distance = findViewById(R.id.distanceEt);
        int distanceHeight = distance.getHeight();
        Switch searchAnyWhereSwitch = findViewById(R.id.locationSwitch);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;
        int height = displayMetrics.heightPixels;
        CardView cardView = findViewById(R.id.popUpCardView);
        //getWindow().setLayout((int)(width*0.8),cardView.getHeight()*2);
        getWindow().setLayout((int)(width*0.8),(int)(height*0.9));
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.gravity = Gravity.CENTER;
        params.x = 0;
        params.y = -20;
        getWindow().setAttributes(params);

        searchAnyWhereSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    distance.setVisibility(View.INVISIBLE);
                    buttonView.setText("Search AnyWhere");
                }
                else {
                    distance.setVisibility(View.VISIBLE);
                    buttonView.setText("Search at maximum distance");
                }

            }
        });
        doneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PopUpActivity.this,MainActivity.class);
                intent.putExtra("Profession",professionEt.getText().toString());
                intent.putExtra("Job Description",jobDesc.getText().toString());
                intent.putExtra("qualifications",qualifications.getText().toString());
                intent.putExtra("note",note.getText().toString());
                intent.putExtra("distance",searchAnyWhereSwitch.isChecked());
                startActivity(intent);
                finish();
            }
        });

    }
}
