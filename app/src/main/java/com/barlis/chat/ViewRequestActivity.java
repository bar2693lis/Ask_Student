package com.barlis.chat;

import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.barlis.chat.Model.ERequestStatus;
import com.barlis.chat.Model.EResultCodes;
import com.barlis.chat.Model.Request;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ViewRequestActivity extends AppCompatActivity {
    Button acceptBtn, backBtn;
    TextView requestTitle, profession, requestDescription, qualifications, notes, requestStatus;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_request);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;
        int height = displayMetrics.heightPixels;
        getWindow().setLayout((int)(width*0.8), (int)(height*0.7));
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.gravity = Gravity.CENTER;
        getWindow().setAttributes(params);

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        requestTitle = findViewById(R.id.request_title);
        profession = findViewById(R.id.needed_profession);
        requestDescription = findViewById(R.id.request_description);
        qualifications = findViewById(R.id.qualifications);
        notes = findViewById(R.id.notes);
        requestStatus = findViewById(R.id.request_status);
        acceptBtn = findViewById(R.id.accept_btn);
        backBtn = findViewById(R.id.back_btn);

        acceptBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("request_position", getIntent().getIntExtra("request_position", 0 ));
                setResult(EResultCodes.UPDATE_REQUEST.getValue(), intent);
                finish();
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Request request = (Request)getIntent().getSerializableExtra("request");
        requestTitle.setText(request.getRequestTitle());
        profession.setText(request.getRequiredProfession());
        requestDescription.setText(request.getRequestDetails());
        qualifications.setText(request.getQualifications());
        notes.setText(request.getNotes());
        requestStatus.setText(request.getStatus() + "");
        removeEmptyFields(request);


        if (firebaseUser.getUid().equals(request.getCreatorId()) || !request.getStatus().equals(ERequestStatus.REQUEST_AVAILABLE)) {
            acceptBtn.setEnabled(false);
        }
    }

    private void removeEmptyFields(Request request) {
        if (request.getQualifications() == null) {
            qualifications.setVisibility(View.GONE);
        }
        if (request.getNotes() == null) {
            notes.setVisibility(View.GONE);
        }
    }
}
