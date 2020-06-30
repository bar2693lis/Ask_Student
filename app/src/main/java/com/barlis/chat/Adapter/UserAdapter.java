package com.barlis.chat.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.barlis.chat.MainActivity;
import com.barlis.chat.MessageActivity;
import com.barlis.chat.Model.Chat;
import com.barlis.chat.Model.User;
import com.barlis.chat.R;
import com.barlis.chat.StartActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    private Context mContext;
    private List<User> mUsers;

    private boolean isChat;

    private boolean isChatLayout;

    String theLastMessage;
    String theLastMessageType;

    public UserAdapter(Context mContext, List<User> mUsers, boolean isChat, boolean isChatLayout){ // Constructor
        this.mContext = mContext;
        this.mUsers = mUsers;
        this.isChat = isChat;
        this.isChatLayout = isChatLayout;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if( isChatLayout == true) // If this is the chat fragment then inflate this cell
        {
            view = LayoutInflater.from(mContext).inflate(R.layout.chat_item, parent, false);
        }
        else // If this is the users fragment then inflate this cell
        {
            view = LayoutInflater.from(mContext).inflate(R.layout.user_cell, parent, false);
        }
        return new UserAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final User user = mUsers.get(position);
        holder.username.setText(user.getUsername());

        if(user.getImageURL().equals("default")){  // When there is no link to the image use the default
            holder.profile_image.setImageResource(R.mipmap.ic_launcher);
        }
        else{
            Picasso.get().load(user.getImageURL()).into(holder.profile_image);
        }

        if(isChatLayout){  // If this is the chat fragment
            if(isChat){ // If you have a chat, you will see the last message
                lastMessage(user.getId(), holder.last_message);
            }
            else{ // If there is no chat then you will not see messages
                holder.last_message.setVisibility(View.GONE);
            }

            if(isChat){ // If you have a chat
                if(user.getStatus().equals("online")){ // If the other user is logged in and he saw the last message then the message becomes "seen"
                    holder.img_on.setVisibility(View.VISIBLE);
                    holder.img_off.setVisibility(View.GONE);
                }
                else{ // If the other user has not seen the last message then the message stay "Delivered"
                    holder.img_on.setVisibility(View.GONE);
                    holder.img_off.setVisibility(View.VISIBLE);
                }
            }
            else{ // If there is no chat then there is nothing to show
                holder.img_on.setVisibility(View.GONE);
                holder.img_off.setVisibility(View.GONE);
            }
        }


        holder.itemView.setOnClickListener(new View.OnClickListener() { // When a certain user is clicked
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, MessageActivity.class);
                intent.putExtra("userId", user.getId());
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public TextView username;
        public ImageView profile_image;

        private ImageView img_on;
        private ImageView img_off;

        private TextView last_message;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            username = itemView.findViewById(R.id.username);
            profile_image = itemView.findViewById(R.id.profile_image);
            img_on = itemView.findViewById(R.id.img_on);
            img_off = itemView.findViewById(R.id.img_off);
            last_message = itemView.findViewById(R.id.last_message);
        }
    }

    // check for last message
    private void lastMessage(final String userId, final TextView last_message){ // Check for last message from the other user and showing it in the chat fragment
        theLastMessage = "default";
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Chat chat = snapshot.getValue(Chat.class);

                    // Prevent NullPointerException
                    if (firebaseUser != null) {
                        if ((chat.getReceiver().equals(firebaseUser.getUid()) && chat.getSender().equals(userId)) || (chat.getReceiver().equals(userId) && chat.getSender().equals(firebaseUser.getUid()))) { // If I sent the last message then it saves it for viewing, if your other user has the last message then it saves it for viewing
                            theLastMessage = chat.getMessage();
                            theLastMessageType = chat.getType();
                        }
                    }
                }

                switch (theLastMessage){
                    case "default": // If there is no message
                       last_message.setText("No Messages");
                       break;

                    default: // Displays the last message sent
                        if (theLastMessageType.equals("image")) {
                            last_message.setText(mContext.getResources().getString(R.string.image));
                        }
                        else {
                            last_message.setText(theLastMessage);
                        }
                        break;
                }

                theLastMessage = "default";
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
