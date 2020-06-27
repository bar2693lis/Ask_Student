package com.barlis.chat.Notification;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.barlis.chat.Fragments.APIService;
import com.barlis.chat.MainActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// A class for building notifications
public class NotificationBuilder {
    public static void sendNotification(Context context, String sender,String receiver, String body, String title){
        DatabaseReference token = FirebaseDatabase.getInstance().getReference("Tokens");

        Query query = token.orderByKey().equalTo(receiver);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Token token = snapshot.getValue(Token.class);

                    Data data = new Data(sender, body, title, receiver);

                    Sender sender = new Sender(data, token.getToken());
                    APIService apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);
                    apiService.sendNotification(sender).enqueue(new Callback<MyResponse>() {
                        @Override
                        public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                            if(response.code() == 200){
                                if(response.body().success != 1){
                                    Toast.makeText(context, "Failed!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<MyResponse> call, Throwable t) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
