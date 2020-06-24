package com.barlis.chat.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.barlis.chat.Adapter.RequestAdapter;
import com.barlis.chat.Model.ERequestStatus;
import com.barlis.chat.Model.Request;
import com.barlis.chat.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RequestsFragment extends Fragment {
    private RecyclerView recyclerView;
    private RequestAdapter requestAdapter;
    private List<Request> requests;

    DatabaseReference requestReference;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_requests, container, false);

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        requests = new ArrayList<>();
        requestReference = FirebaseDatabase.getInstance().getReference("Requests");
        requestReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Request request = snapshot.getValue(Request.class);
                    request.setRequestId(snapshot.getKey());
                    requests.add(request);
                }
                requestAdapter = new RequestAdapter(getContext(), requests);
                recyclerView.setAdapter(requestAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        return view;
    }

    public void addRequest(Request request) {
        requestReference = FirebaseDatabase.getInstance().getReference("Requests");
        String key = requestReference.push().getKey();
        request.setRequestId(key);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("creatorId", request.getCreatorId());
        hashMap.put("creatorName", request.getCreatorName());
        hashMap.put("notes", request.getNotes());
        hashMap.put("qualifications", request.getQualifications());
        hashMap.put("requestDetails", request.getRequestDetails());
        hashMap.put("requestTitle", request.getRequestTitle());
        hashMap.put("requiredProfession", request.getRequiredProfession());
        hashMap.put("status", request.getStatus());
        requestReference.child(key).updateChildren(hashMap);
        if (requests != null) {
            requests.add(request);
            requestAdapter.notifyItemInserted(requests.size() - 1);
        }
    }

    public void updateRequest(int position, String workerName, String workerId) {
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
}
