package com.barlis.chat.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.barlis.chat.Model.Chat;
import com.barlis.chat.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    public static final int MSG_TYPE_LEFT = 0; // My message
    public static final int MSG_TYPE_RIGHT = 1; // His message

    private Context mContext;
    private List<Chat> chat_list;
    private String image_url;

    FirebaseUser firebaseUser;

    public MessageAdapter(Context mContext, List<Chat> chat_list, String image_url){ // Constructor
        this.mContext = mContext;
        this.chat_list = chat_list;
        this.image_url = image_url;
    }

    @NonNull
    @Override
    public MessageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == MSG_TYPE_RIGHT){
            View view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_right, parent, false); // If this is my message then inflate the right balloon
            return new MessageAdapter.ViewHolder(view);
        }
        else{
            View view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_left, parent, false); // If this is his message then inflate the left balloon
            return new MessageAdapter.ViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MessageAdapter.ViewHolder holder, int position) {
        Chat chat = chat_list.get(position);

        holder.show_message_tv.setText(chat.getMessage());

        if(image_url.equals("default")){ // When there is no link to the image use the default
            holder.profile_image.setImageResource(R.drawable.user);
        }
        else{
            Picasso.get().load(image_url).into(holder.profile_image);
        }

        if(position == chat_list.size()-1){ // Check for last message

            if(chat.isIsseen()){ // Checks if the last message was read by the other user
                holder.text_seen_tv.setText("Seen");
            }
            else{
                holder.text_seen_tv.setText("Delivered");
            }
        }
        else{ // There is no message
            holder.text_seen_tv.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return chat_list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public TextView show_message_tv, text_seen_tv;
        public ImageView profile_image;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            show_message_tv = itemView.findViewById(R.id.show_message);
            profile_image = itemView.findViewById(R.id.profile_image);
            text_seen_tv = itemView.findViewById(R.id.txt_seen);
        }
    }

    @Override
    public int getItemViewType(int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if(chat_list.get(position).getSender().equals(firebaseUser.getUid())){
            return MSG_TYPE_RIGHT;
        }
        else{
            return MSG_TYPE_LEFT;
        }
    }
}