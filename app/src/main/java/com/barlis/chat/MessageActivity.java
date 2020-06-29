package com.barlis.chat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.barlis.chat.Adapter.MessageAdapter;
import com.barlis.chat.Fragments.APIService;
import com.barlis.chat.Model.Chat;
import com.barlis.chat.Model.ERequestCodes;
import com.barlis.chat.Model.User;
import com.barlis.chat.Notification.Client;
import com.barlis.chat.Notification.Data;
import com.barlis.chat.Notification.MyResponse;
import com.barlis.chat.Notification.NotificationBuilder;
import com.barlis.chat.Notification.Sender;
import com.barlis.chat.Notification.Token;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MessageActivity extends AppCompatActivity {

    final int ATTACH_FILE_REQUEST = 1;
    final int ATTACH_PICTURE_REQUEST = 2;
    final int GET_PICTURE_REQUEST = 3;

    File imageFile;
    Uri imageUri;
    CircleImageView profile_image;
    TextView username;

    FirebaseUser fuser;
    DatabaseReference reference;
    StorageReference storageReference;
    StorageTask<UploadTask.TaskSnapshot> uploadTask;

    ImageButton btn_send;
    EditText txt_send;

    MessageAdapter messageAdapter;
    List<Chat> mChat;

    RecyclerView recyclerView;

    Intent intent;

    ValueEventListener seenListener;

    String userId;

    APIService apiService;

    boolean notify = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MessageActivity.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            }
        });

        storageReference = FirebaseStorage.getInstance().getReference("Uploads");

        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);

        recyclerView.setLayoutManager(linearLayoutManager);


        profile_image = findViewById(R.id.profile_image);
        username = findViewById(R.id.username);
        btn_send = findViewById(R.id.btn_send);
        txt_send = findViewById(R.id.txt_send);

        intent = getIntent();
        userId = intent.getStringExtra("userId");

        fuser = FirebaseAuth.getInstance().getCurrentUser();

        btn_send.setOnClickListener(new View.OnClickListener() { // Send message button
            @Override
            public void onClick(View v) {
                notify = true;
                String msg = txt_send.getText().toString();
                if(!msg.equals("")){ // If the message is not empty
                    sendMessage(fuser.getUid(), userId, msg, "text");
                }
                else{ // If the message is blank
                    Toast.makeText(MessageActivity.this, getResources().getString(R.string.empty_message_alert), Toast.LENGTH_SHORT).show();
                }

                txt_send.setText("");
            }
        });

        ImageButton btn_attach = findViewById(R.id.btn_attach);
        btn_attach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(MessageActivity.this, v);
                try {
                    Field[] fields = popup.getClass().getDeclaredFields();
                    for (Field field : fields) {
                        if ("mPopup".equals(field.getName())) {
                            field.setAccessible(true);
                            Object menuPopupHelper = field.get(popup);
                            Class<?> classPopupHelper = Class.forName(menuPopupHelper.getClass().getName());
                            Method setForceIcons = classPopupHelper.getMethod("setForceShowIcon", boolean.class);
                            setForceIcons.invoke(menuPopupHelper, true);
                            break;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                popup.getMenuInflater().inflate(R.menu.attach_menu, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.attach_file:
                                if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                                    getFile(new String[] {"application/*", "text/*"}, ATTACH_FILE_REQUEST);
                                }
                                else {
                                    requestPermissions(new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, ATTACH_FILE_REQUEST);
                                }
                                return true;
                            case R.id.attach_picture:
                                if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                                    getFile(new String[] {"image/*"}, ATTACH_PICTURE_REQUEST);
                                }
                                else {
                                    requestPermissions(new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, ATTACH_PICTURE_REQUEST);
                                }
                                return true;
                            case R.id.take_picture:
                                imageFile = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), UUID.randomUUID() + ".png");
                                imageUri = FileProvider.getUriForFile(MessageActivity.this, "com.barlis.chat.provider", imageFile);
                                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                                startActivityForResult(intent, GET_PICTURE_REQUEST);
                                return true;
                        }
                        return false;
                    }
                });
                popup.show();
            }
        });

        reference = FirebaseDatabase.getInstance().getReference("Users").child(userId);
        reference.addValueEventListener(new ValueEventListener() { // reference to the information of the current user
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                username.setText(user.getUsername());

                if(user.getImageURL().equals("default")){ // When there is no link to the image use the default
                    profile_image.setImageResource(R.mipmap.ic_launcher);
                }
                else{
                    Picasso.get().load(user.getImageURL()).into(profile_image);
                }

                toolbar.setOnClickListener(new View.OnClickListener() { // When you click on the other user's image or name in the toolbar
                    @Override
                    public void onClick(View v) {
                        Intent viewProfileIntent = new Intent(getApplicationContext(), ViewUserProfileActivity.class);
                        viewProfileIntent.putExtra("userId", userId);
                        startActivity(viewProfileIntent);
                    }
                });
                readMessage(fuser.getUid(), userId, user.getImageURL());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        seenMessage(userId);
    }

    private void seenMessage(final String userId){ // Checks if the other user saw the message
        reference = FirebaseDatabase.getInstance().getReference("Chats");
        seenListener = reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Chat chat = snapshot.getValue(Chat.class);

                    if(chat.getReceiver().equals(fuser.getUid()) && chat.getSender().equals(userId)){ // Checks whether the current user id and the other user id match the same chat
                        HashMap<String, Object> hasMap = new HashMap<>();
                        hasMap.put("isseen", true);
                        snapshot.getRef().updateChildren(hasMap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendMessage(String sender, String receiver,String message, String type){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        HashMap<String, Object> hashMap = new HashMap<>();

        hashMap.put("sender", sender);
        hashMap.put("receiver", receiver);
        hashMap.put("message", message);
        hashMap.put("isseen", false);
        hashMap.put("type", type);

        reference.child("Chats").push().setValue(hashMap);

        // Add current user to chat fragment
        final DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("ChatList").child(fuser.getUid()).child(userId);

        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists()){
                    chatRef.child("id").setValue(userId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        // Add recipient user to chat fragment
        final DatabaseReference chatRevRef = FirebaseDatabase.getInstance().getReference("ChatList").child(userId).child(fuser.getUid());

        chatRevRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists()){
                    chatRevRef.child("id").setValue(fuser.getUid());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        final String msg = message;

        reference = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);

                if(notify){
                    sendNotification(receiver, user.getUsername(), msg);
                }
                notify = false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendNotification(String receiver, String sentFrom, String message){
        DatabaseReference token = FirebaseDatabase.getInstance().getReference("Tokens");

        Query query = token.orderByKey().equalTo(receiver);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Token token = snapshot.getValue(Token.class);

                    Data data = new Data(fuser.getUid(), message,  getResources().getString(R.string.new_message_alert)+ " " + sentFrom , userId);

                    Sender sender = new Sender(data, token.getToken());

                    apiService.sendNotification(sender).enqueue(new Callback<MyResponse>() {
                        @Override
                        public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                            if(response.code() == 200){
                                if(response.body().success != 1){
                                    Toast.makeText(MessageActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
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

    private void readMessage(final String myId, final String userId, final String imageURL){
        mChat = new ArrayList<>();

        reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mChat.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Chat chat = snapshot.getValue(Chat.class);

                    if(chat.getReceiver().equals(myId) && chat.getSender().equals(userId) || chat.getReceiver().equals(userId) && chat.getSender().equals(myId)){
                        mChat.add(chat);
                    }

                    messageAdapter = new MessageAdapter(MessageActivity.this, mChat, imageURL);
                    recyclerView.setAdapter(messageAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void chattingWithCurrentUser(String userId){
        SharedPreferences.Editor editor = getSharedPreferences("PREFS", MODE_PRIVATE).edit();
        editor.putString("currentUser", userId);
        editor.apply();
    }

    private void status(String status){
        reference = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status", status);

        reference.updateChildren(hashMap);
    }

    @Override
    protected void onResume() {
        super.onResume();
        status("online");
        chattingWithCurrentUser(userId);
    }

    @Override
    protected void onPause() {
        super.onPause();
        reference.removeEventListener(seenListener);
        status("offline");
        chattingWithCurrentUser("none");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == ATTACH_FILE_REQUEST) {
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getFile(new String[] {"application/*", "text/*"}, ATTACH_FILE_REQUEST);
            }
        }
        else if (requestCode == ATTACH_PICTURE_REQUEST) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getFile(new String[] {"image/*"}, ATTACH_PICTURE_REQUEST);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == ATTACH_FILE_REQUEST) {
            if (data != null) {
                Uri fileUri = data.getData();
                if (fileUri != null) {
                    uploadFile(fileUri, "file");
                }
            }
        }
        else if (requestCode == ATTACH_PICTURE_REQUEST) {
            if (data != null) {
                Uri fileUri = data.getData();
                if (fileUri != null) {
                    uploadFile(fileUri, "image");
                }
            }
        }
        else if (requestCode == GET_PICTURE_REQUEST && resultCode == RESULT_OK) {
            uploadFile(imageUri, "image");
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void uploadFile(Uri fileUri, String type) {
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage(getResources().getString(R.string.upload_in_progress));
        pd.show();

        if(fileUri != null){ // If the uri is not empty
            final StorageReference fileReference = storageReference.child(System.currentTimeMillis() + "." + getFileExtension(fileUri));

            uploadTask = fileReference.putFile(fileUri); // To upload a file to Cloud Storage, you first create a reference to the full path of the file, including the file name
            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() { // Returns a new Task that will be completed with the result of applying the specified Continuation to this Task (The Continuation will be called on the main application thread)
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if(!task.isSuccessful()){ // If the task fails
                        throw Objects.requireNonNull(task.getException());
                    }

                    return fileReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() { // To handle success and failure in the same listener
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if(task.isSuccessful()){ // If the task was successful
                        Uri downloadUri = task.getResult();
                        assert downloadUri != null;
                        String strUri = downloadUri.toString();

                        sendMessage(fuser.getUid(), userId, strUri, type);

                        pd.dismiss();
                    }
                    else{ // If image upload failed
                        Toast.makeText(MessageActivity.this, getResources().getString(R.string.upload_failed), Toast.LENGTH_SHORT).show();
                        pd.dismiss();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() { // To be notified when the task fails
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(MessageActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    pd.dismiss();
                }
            });
        }
        else{ // If no image is selected
            Toast.makeText(this, getResources().getString(R.string.no_image_selected), Toast.LENGTH_SHORT).show();
            pd.dismiss();
        }
    }

    private void getFile(String[] mimtypes, int requestCode) {
        Intent intent=new Intent();
        intent.setType("*/*");
        String[] mimetypes = {"image/*", "application/*", "text/*"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, requestCode);
    }

    private String getFileExtension(Uri uri){
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }
}
