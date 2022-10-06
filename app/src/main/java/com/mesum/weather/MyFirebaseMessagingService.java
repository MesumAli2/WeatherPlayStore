package com.mesum.weather;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Objects;




public class MyFirebaseMessagingService extends FirebaseMessagingService {



    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);
        Log.d("FirebaseClassMessage", "From: " + Objects.requireNonNull(message.getNotification()).getBody().toString());
    }




}
