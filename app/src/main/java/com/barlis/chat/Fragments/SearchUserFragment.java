package com.barlis.chat.Fragments;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.barlis.chat.MainActivity;
import com.barlis.chat.Model.EResultCodes;
import com.barlis.chat.R;

public class SearchUserFragment extends Fragment {
    EditText professionEt, distance;
    Switch searchAnyWhereSwitch;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search_user, container, false);
        professionEt = view.findViewById(R.id.professionEt);
        Button doneBtn = view.findViewById(R.id.doneBtn);
        distance = view.findViewById(R.id.distanceEt);
        searchAnyWhereSwitch = view.findViewById(R.id.locationSwitch);

        searchAnyWhereSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    distance.setVisibility(View.INVISIBLE);
                    buttonView.setText(getResources().getString(R.string.search_anywhere));
                }
                else {
                    distance.setVisibility(View.VISIBLE);
                    buttonView.setText(getResources().getString(R.string.search_at_maximum_distance));
                }
            }
        });

        doneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!professionEt.getText().toString().isEmpty()) {

                    if (!searchAnyWhereSwitch.isChecked() && distance.getText().toString().isEmpty()) {
                        Toast.makeText(getContext(), getResources().getString(R.string.missing_fields), Toast.LENGTH_SHORT).show();
                        markMissingFields();
                    }
                    else {
                        boolean searchByDistance = false;
                        Intent intent = new Intent();
                        intent.putExtra("profession", professionEt.getText().toString().trim().toLowerCase());
                        if (!searchAnyWhereSwitch.isChecked()) {
                            searchByDistance = true;
                            intent.putExtra("distance", Integer.parseInt(distance.getText().toString()));
                        }
                        intent.putExtra("searchByDistance", searchByDistance);
                        getActivity().setResult(EResultCodes.SEARCH_USER.getValue(), intent);
                        getActivity().finish();
                    }
                }
            }
        });
        return view;
    }

    // Mark empty fields in red
    private void markMissingFields() {
        if (professionEt.getText().toString().isEmpty()) {
            professionEt.setHintTextColor(Color.RED);
        }
        else {
            professionEt.setHintTextColor(Color.BLACK);
        }
        if (searchAnyWhereSwitch.isChecked()) {
            if (distance.getText().toString().isEmpty()) {
                distance.setHintTextColor(Color.RED);
            }
            else {
                distance.setHintTextColor(Color.BLACK);
            }
        }
    }
}
