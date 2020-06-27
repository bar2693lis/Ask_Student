package com.barlis.chat.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

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
    private Spinner statusFilter, professionFilter;
    private ArrayAdapter<String> statusAdapter, professionAdapter;
    private DatabaseReference requestReference;
    private int statusFilterLastPosition = 0;
    private int professionFilterLastPosition = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_requests, container, false);

        recyclerView = view.findViewById(R.id.recycler_view);
        professionFilter = view.findViewById(R.id.profession_filter);
        statusFilter = view.findViewById(R.id.status_filter);
        // Status array for filter
        String[] statusArr = new String[] {getResources().getString(R.string.any_status), getResources().getString(R.string.status_available), getResources().getString(R.string.status_taken), getResources().getString(R.string.status_done)};
        statusAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_dropdown_item, statusArr);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        requests = new ArrayList<>();
        requestReference = FirebaseDatabase.getInstance().getReference("Requests");
        requestReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                requests.clear();
                // Collect all professions for filter
                List<String> professions = new ArrayList<>();
                professions.add(getResources().getString(R.string.any_profession));
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Request request = snapshot.getValue(Request.class);
                    request.setRequestId(snapshot.getKey());
                    // Add only if not duplicate
                    if (!professions.contains(request.getRequiredProfession().toLowerCase())) {
                        professions.add(request.getRequiredProfession().toLowerCase());
                    }
                    requests.add(request);
                }
                professionAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_dropdown_item, professions.toArray(new String[professions.size()]));
                requestAdapter = new RequestAdapter(getContext(), requests);
                recyclerView.setAdapter(requestAdapter);
                statusFilter.setAdapter(statusAdapter);
                professionFilter.setAdapter(professionAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        statusFilter.setSelection(0, false);
        statusFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Prevent activation on create
                if (statusFilterLastPosition != position) {
                    statusFilterLastPosition = position;
                    filterRequests();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        professionFilter.setSelection(0, false);
        professionFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Prevent activation on create
                if (professionFilterLastPosition != position) {
                    professionFilterLastPosition = position;
                    filterRequests();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        return view;
    }

    private void filterRequests() {
        // Check if status or profession filters were selected
        final boolean isFilterStatus = statusFilter.getSelectedItemPosition() != 0;
        final boolean isFilterProfession = professionFilter.getSelectedItemPosition() != 0;

        requestReference = FirebaseDatabase.getInstance().getReference("Requests");
        requests.clear();
        requestReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                    Request request = snapshot.getValue(Request.class);
                    int passedFilters = 0;
                    if (isFilterStatus) {
                        if (request.getStatus().getValue() == statusFilter.getSelectedItemPosition()) {
                            passedFilters++;
                        }
                    }
                    else {
                        passedFilters++;
                    }

                    if (isFilterProfession) {
                        if (request.getRequiredProfession().toLowerCase().equals(professionFilter.getSelectedItem().toString().toLowerCase())) {
                            passedFilters++;
                        }
                    }
                    else {
                        passedFilters++;
                    }

                    // Add request if passed both filters
                    if (passedFilters == 2) {
                        request.setRequestId(snapshot.getKey());
                        requests.add(request);
                    }
                }
                requestAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    // Add request to list, update DB and recycler
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
