package com.barlis.chat.Adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.barlis.chat.Model.ERequestCodes;
import com.barlis.chat.Model.Request;
import com.barlis.chat.R;
import com.barlis.chat.ViewRequestActivity;

import org.w3c.dom.Text;

import java.util.List;

public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.ViewHolder> {
    private Context context;
    private List<Request> requests;

    public RequestAdapter(Context context, List<Request> requests) {
        this.context = context;
        this.requests = requests;
    }

    @NonNull
    @Override
    public RequestAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.request_item, parent, false);
        return new RequestAdapter.ViewHolder(view);
    }



    @Override
    public void onBindViewHolder(@NonNull RequestAdapter.ViewHolder holder, int position) {
        final Request request = requests.get(position);
        holder.username.setText(request.getCreatorName());
        holder.requestTitle.setText(request.getRequestTitle());
        switch (request.getStatus()) {
            case REQUEST_AVAILABLE:
                holder.requestStatus.setText(context.getResources().getString(R.string.status_available));
                break;
            case REQUEST_TAKEN:
                holder.requestStatus.setText(context.getResources().getString(R.string.status_taken));
                break;
            case REQUEST_DONE:
                holder.requestStatus.setText(context.getResources().getString(R.string.status_done));
                break;

            default:
                holder.requestStatus.setText(request.getStatus() + "");
                break;
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ViewRequestActivity.class);
                intent.putExtra("request", request);
                intent.putExtra("request_position", position);
                ((Activity)context).startActivityForResult(intent, ERequestCodes.UPDATE_REQUEST.getValue());
            }
        });
    }

    @Override
    public int getItemCount() {
        return requests.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public TextView requestTitle;
        public TextView username;
        public ImageView profileImage;
        public TextView requestStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            username = itemView.findViewById(R.id.username);
            requestTitle = itemView.findViewById(R.id.request_title);
            requestStatus = itemView.findViewById(R.id.request_status);
            profileImage = itemView.findViewById(R.id.profile_image);
        }
    }
}
