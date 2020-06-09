package com.barlis.chat.Fragments;

import com.barlis.chat.Notification.MyResponse;
import com.barlis.chat.Notification.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {

    @Headers({"Content-Type:application/json", "Authorization:key=AAAA6uqBwQU:APA91bHbDVDFeGE909O3BBFaqkcsLuPZ1wLKNfqbq3GJAYIb1regEXOmmQjt8hVOLOWoVLII4AIWy-XlcewK1R3w6bNYbQG6Zq8t-MzoRwmWa0jhix-ryt5x9WIYqtop3quQE2q7RvHY"})

    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);
}
