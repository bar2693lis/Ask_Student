package com.barlis.chat.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.barlis.chat.R;

public class OpenRequestFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_open_request, container, false);
        EditText professionEt = view.findViewById(R.id.professionEt);
        EditText jobDesc = view.findViewById(R.id.jobDesc);
        EditText qualifications = view.findViewById(R.id.qualificationsEt);
        EditText note = view.findViewById(R.id.note);
        Button doneBtn = view.findViewById(R.id.doneBtn);
        EditText distance = view.findViewById(R.id.distanceEt);
        Switch searchAnyWhereSwitch = view.findViewById(R.id.locationSwitch);

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

            }
        });
        return view;
    }
}
