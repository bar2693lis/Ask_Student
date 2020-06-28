package com.barlis.chat;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.barlis.chat.Adapter.RequestAdapter;
import com.barlis.chat.Model.ERequestCodes;
import com.barlis.chat.Model.ERequestStatus;
import com.barlis.chat.Model.EResultCodes;
import com.barlis.chat.Model.Request;
import com.barlis.chat.Notification.NotificationBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class UserSpecificRequestsFragment extends Fragment {

    private RecyclerView recyclerView;
    private RequestAdapter requestAdapter;
    private List<Request> requests;
    private CheckBox getOpenedRequests, getTakenRequests;
    private DatabaseReference requestReference;
    private FirebaseUser currentUser;
    private String userName;
    public UserSpecificRequestsFragment() {
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_specific_requests, container, false);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        recyclerView = view.findViewById(R.id.recycler_view);
        getOpenedRequests = view.findViewById(R.id.opened_requests_cb);
        getTakenRequests = view.findViewById(R.id.taken_requests_cb);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
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
        return view;
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
                requestAdapter = new RequestAdapter(getContext(), requests);
                recyclerView.setAdapter(requestAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    // Change request at specific position to done and update recycler
    public void closeRequest(int position, String workerId) {
        requests.get(position).setStatus(ERequestStatus.REQUEST_DONE);
        FirebaseDatabase.getInstance().getReference("Requests").child(requests.get(position).getRequestId()).child("status").setValue(ERequestStatus.REQUEST_DONE);
        NotificationBuilder.sendNotification(getContext(), currentUser.getUid(), workerId, userName + " " + getResources().getString(R.string.request_closed_alert), getResources().getString(R.string.request_update_alert));
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
        NotificationBuilder.sendNotification(getContext(), currentUser.getUid(), workerId, userName + " " + getResources().getString(R.string.removed_from_request_alert), getResources().getString(R.string.request_update_alert));
    }

    // Remove worker from request and send the creator a notification
    public void quitFromRequest(int position, String creatorId) {
        removeWorkerFromRequest(position, currentUser.getUid());
        NotificationBuilder.sendNotification(getContext(), currentUser.getUid(), creatorId, userName + " " + getResources().getString(R.string.worker_quit_alert), getResources().getString(R.string.request_update_alert));

    }
}
