package com.barlis.chat.Fragments;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.barlis.chat.R;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class RateUserDialogFragment extends DialogFragment implements View.OnClickListener {
    String userName, imageUrl;
    AlertDialog alertDialog;
    View dialogView;
    MyDialogListener callback;
    Button submitRating;
    ImageView stars[];
    CircleImageView profileImg;
    TextView title;
    int userRating, previousRating;

    public interface MyDialogListener {
        void onReturn(int rating, int oldRating);
    }

    public RateUserDialogFragment(String userName, String imageUrl, int previousRating) {
        this.userName = userName;
        this.imageUrl = imageUrl;
        this.previousRating = previousRating;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            callback = (MyDialogListener)context;
        } catch (ClassCastException ex) {
            throw new ClassCastException("The activity must implement MyDialogListener interface");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        dialogView = inflater.inflate(R.layout.fragment_rate_user, (ViewGroup) getActivity().findViewById(R.id.user_profile_layout), false);
        builder.setView(dialogView);
        alertDialog = builder.create();
        stars = new ImageView[5];
        stars[0] = dialogView.findViewById(R.id.star_one);
        stars[1] = dialogView.findViewById(R.id.star_two);
        stars[2] = dialogView.findViewById(R.id.star_three);
        stars[3] = dialogView.findViewById(R.id.star_four);
        stars[4] = dialogView.findViewById(R.id.star_five);
        setStars(previousRating);
        userRating = previousRating;
        submitRating = dialogView.findViewById(R.id.btn_submit);
        profileImg = dialogView.findViewById(R.id.profile_image);
        title = dialogView.findViewById(R.id.title);

        title.setText(getResources().getString(R.string.rate) + " " + userName);
        if (imageUrl.equals("default")) {
            profileImg.setImageResource(R.mipmap.ic_launcher);
        }
        else {
            Picasso.get().load(imageUrl).into(profileImg);
        }
        for (int i = 0; i < 5; i++) {
            stars[i].setOnClickListener(this);
        }
        submitRating.setOnClickListener(this);

        return alertDialog;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_submit) {
            if (userRating != 0) {
                callback.onReturn(userRating, previousRating);
                alertDialog.dismiss();
            }
            else {
                Toast.makeText(dialogView.getContext(), getResources().getString(R.string.please_choose_rating), Toast.LENGTH_SHORT).show();
            }
        }
        else {
            userRating = 0;
            switch (v.getId()) {
                case R.id.star_one:
                    userRating = 1;
                    break;
                case R.id.star_two:
                    userRating = 2;
                    break;
                case R.id.star_three:
                    userRating = 3;
                    break;
                case R.id.star_four:
                    userRating = 4;
                    break;
                case R.id.star_five:
                    userRating = 5;
                    break;
            }

            setStars(userRating);
        }
    }

    private void setStars(int numOfStars) {
        for (int i = 0; i < numOfStars; i++) {
            stars[i].setImageResource(R.drawable.full_star);
        }
        for (int i = numOfStars; i < 5; i++) {
            stars[i].setImageResource(R.drawable.empty_star);
        }
    }
}
