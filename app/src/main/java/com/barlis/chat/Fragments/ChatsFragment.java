package com.barlis.chat.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.barlis.chat.Adapter.UserAdapter;
import com.barlis.chat.Model.Chat;
import com.barlis.chat.Model.ChatList;
import com.barlis.chat.Model.User;
import com.barlis.chat.Notification.Token;
import com.barlis.chat.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.ArrayList;
import java.util.List;


public class ChatsFragment extends Fragment {

    private RecyclerView recyclerView;

    private UserAdapter userAdapter;
    private List<User> users_list;
    private List<ChatList> user_chat_list;

    FirebaseUser firebaseUser;
    DatabaseReference databaseReference;

    public ChatsFragment() { // Default constructor
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chats, container, false);

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        user_chat_list = new ArrayList<>();

        databaseReference = FirebaseDatabase.getInstance().getReference("ChatList").child(firebaseUser.getUid());
        databaseReference.addValueEventListener(new ValueEventListener() { // Collects all sent and received messages to a list for the current user
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                user_chat_list.clear();

                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    ChatList chatlist = snapshot.getValue(ChatList.class);
                    user_chat_list.add(chatlist);
                }
                
                chatList();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(getActivity(),  new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                String token = instanceIdResult.getToken();
                updateToken(token);
            }
        });

        return view;
    }

    private void updateToken(String token){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Tokens");
        Token token1 = new Token(token);
        reference.child(firebaseUser.getUid()).setValue(token1);
    }

    private void chatList() { // Gathers the users who are having a conversation with the current user
        users_list = new ArrayList<>();

        databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                users_list.clear();

                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    User user = snapshot.getValue(User.class);

                    for(ChatList chatlist : user_chat_list){
                        if(user.getId().equals(chatlist.getId())){
                            users_list.add(user);
                        }
                    }
                }
                userAdapter = new UserAdapter(getContext(), users_list, true, true);
                recyclerView.setAdapter(userAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
