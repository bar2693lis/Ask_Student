package com.barlis.chat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.barlis.chat.Adapter.RequestAdapter;
import com.barlis.chat.Fragments.APIService;
import com.barlis.chat.Model.ERequestCodes;
import com.barlis.chat.Model.ERequestStatus;
import com.barlis.chat.Model.EResultCodes;
import com.barlis.chat.Model.Request;
import com.barlis.chat.Notification.Client;
import com.barlis.chat.Notification.Data;
import com.barlis.chat.Notification.MyResponse;
import com.barlis.chat.Notification.NotificationBuilder;
import com.barlis.chat.Notification.Sender;
import com.barlis.chat.Notification.Token;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserSpecificRequestsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RequestAdapter requestAdapter;
    private List<Request> requests;
    private CheckBox getOpenedRequests, getTakenRequests;
    private DatabaseReference requestReference;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_specific_requests);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        recyclerView = findViewById(R.id.recycler_view);
        getOpenedRequests = findViewById(R.id.opened_requests_cb);
        getTakenRequests = findViewById(R.id.taken_requests_cb);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        requests = new ArrayList<>();
        requestReference = FirebaseDatabase.getInstance().getReference("Requests");
        requestReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Put all requests in recyclerView
                readRequests();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        getOpenedRequests.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                readRequests();
            }
        });

        getTakenRequests.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                readRequests();
            }
        });
    }

    private void readRequests() {
        // Take checkbox state
        final boolean isFilterOpenedRequests = getOpenedRequests.isChecked();
        final boolean isFilterTakenRequests = getTakenRequests.isChecked();

        requestReference = FirebaseDatabase.getInstance().getReference("Requests");
        requests.clear();
        requestReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                    Request request = snapshot.getValue(Request.class);
                    // Filter opened requests or taken requests by checkboxes state
                    if (isFilterOpenedRequests && request.getCreatorId().equals(currentUser.getUid())) {
                        request.setRequestId(snapshot.getKey());
                        requests.add(request);
                    }

                    // Worker id will be null if request is still open
                    if (isFilterTakenRequests && request.getWorkerId() != null) {
                        if (request.getWorkerId().equals(currentUser.getUid())) {
                            request.setRequestId(snapshot.getKey());
                            requests.add(request);
                        }
                    }
                }
                requestAdapter = new RequestAdapter(UserSpecificRequestsActivity.this, requests);
                recyclerView.setAdapter(requestAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ERequestCodes.UPDATE_REQUEST.getValue()) {
            if (resultCode == EResultCodes.UPDATE_REQUEST_WORKER.getValue()) {
                // Add worker to request and send that user a notification
                updateRequestWorker(data.getIntExtra("request_position", 0), getIntent().getStringExtra("user_name"), currentUser.getUid());
                Intent intent = new Intent(UserSpecificRequestsActivity.this, MessageActivity.class);
                intent.putExtra("userId", data.getStringExtra("creatorId"));
                startActivity(intent);
                NotificationBuilder.sendNotification(this, currentUser.getUid(), data.getStringExtra("creatorId"), getResources().getString(R.string.job_taken_message_body), getIntent().getStringExtra("user_name") + " " + getResources().getString(R.string.job_taken_notification));
            }
            else if (resultCode == EResultCodes.REMOVE_WORKER.getValue()) {
                // Remove worker from request and send that user a notification
                removeWorkerFromRequest(data.getIntExtra("request_position", 0), data.getStringExtra("workerId"));
                NotificationBuilder.sendNotification(this, currentUser.getUid(), data.getStringExtra("workerId"), getIntent().getStringExtra("user_name") + " " + getResources().getString(R.string.removed_from_request_alert), getResources().getString(R.string.request_update_alert));
            }
            else if (resultCode == EResultCodes.QUIT_REQUEST.getValue()) {
                // Remove worker from request and send the creator a notification
                removeWorkerFromRequest(data.getIntExtra("request_position", 0), currentUser.getUid());
                NotificationBuilder.sendNotification(this, currentUser.getUid(), data.getStringExtra("creatorId"), getIntent().getStringExtra("user_name") + " " + getResources().getString(R.string.worker_quit_alert), getResources().getString(R.string.request_update_alert));
            }
            else if (resultCode == EResultCodes.CLOSE_REQUEST.getValue()) {
                // Close request and send the worker a notification
                closeRequest(data.getIntExtra("request_position",0));
                NotificationBuilder.sendNotification(this, currentUser.getUid(), data.getStringExtra("workerId"), getIntent().getStringExtra("user_name") + " " + getResources().getString(R.string.request_closed_alert), getResources().getString(R.string.request_update_alert));
            }
        }
    }

    // Add a worker to request at specific position, change state to taken and update recycler
    public void updateRequestWorker(int position, String workerName, String workerId) {
        requests.get(position).setStatus(ERequestStatus.REQUEST_TAKEN);
        requests.get(position).setWorkerName(workerName);
        requests.get(position).setWorkerId(workerId);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status", ERequestStatus.REQUEST_TAKEN);
        hashMap.put("workerId", workerId);
        hashMap.put("workerName", workerName);
        FirebaseDatabase.getInstance().getReference("Requests").child(requests.get(position).getRequestId()).updateChildren(hashMap);
        requestAdapter.notifyItemChanged(position);
    }

    // Change request at specific position to done and update recycler
    public void closeRequest(int position) {
        requests.get(position).setStatus(ERequestStatus.REQUEST_DONE);
        FirebaseDatabase.getInstance().getReference("Requests").child(requests.get(position).getRequestId()).child("status").setValue(ERequestStatus.REQUEST_DONE);
    }

    // Remove a worker from request at specific position, change state to available and update recycler
    public void removeWorkerFromRequest(int position, String workerId) {
        DatabaseReference requestReference = FirebaseDatabase.getInstance().getReference("Requests").child(requests.get(position).getRequestId());
        requestReference.child("workerId").removeValue();
        requestReference.child("workerName").removeValue();
        requestReference.child("status").setValue(ERequestStatus.REQUEST_AVAILABLE);
        requests.get(position).setStatus(ERequestStatus.REQUEST_AVAILABLE);
        requests.get(position).setWorkerName("");
        requests.get(position).setWorkerId("");
        requestAdapter.notifyItemChanged(position);
    }
}
