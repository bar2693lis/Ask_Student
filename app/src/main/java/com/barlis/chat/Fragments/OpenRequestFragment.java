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
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.barlis.chat.Model.EResultCodes;
import com.barlis.chat.Model.Request;
import com.barlis.chat.R;

public class OpenRequestFragment extends Fragment {
    EditText requestTitle, jobDesc, professionEt;

    public OpenRequestFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_open_request, container, false);
        professionEt = view.findViewById(R.id.professionEt);
        jobDesc = view.findViewById(R.id.jobDesc);
        EditText qualifications = view.findViewById(R.id.qualificationsEt);
        requestTitle = view.findViewById(R.id.requestTitleEt);
        EditText note = view.findViewById(R.id.note);
        Button doneBtn = view.findViewById(R.id.doneBtn);

        doneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (requestTitle.getText().toString().isEmpty() || jobDesc.getText().toString().isEmpty() ||
                jobDesc.getText().toString().isEmpty()) {
                    Toast.makeText(getContext(), getResources().getString(R.string.missing_fields), Toast.LENGTH_SHORT).show();
                    markMissingFields();
                }
                else {
                    Request request = new Request(requestTitle.getText().toString().trim(), jobDesc.getText().toString().trim(), "", professionEt.getText().toString().trim(), "", qualifications.getText().toString().trim(), note.getText().toString().trim());
                    Intent intent = new Intent();
                    intent.putExtra("request", request);
                    getActivity().setResult(EResultCodes.OPEN_REQUEST.getValue(), intent);
                    getActivity().finish();
                }
            }
        });

        return view;
    }

    private void markMissingFields() {
        if (requestTitle.getText().toString().isEmpty()) {
            requestTitle.setHintTextColor(Color.RED);
        }
        else {
            requestTitle.setHintTextColor(Color.BLACK);
        }
        if (jobDesc.getText().toString().isEmpty()) {
            jobDesc.setHintTextColor(Color.RED);
        }
        else {
            jobDesc.setHintTextColor(Color.BLACK);
        }
        if (professionEt.getText().toString().isEmpty()) {
            professionEt.setHintTextColor(Color.RED);
        }
        else {
            professionEt.setHintTextColor(Color.BLACK);
        }
    }
}
