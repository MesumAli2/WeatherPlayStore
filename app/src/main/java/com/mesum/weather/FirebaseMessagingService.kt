package com.mesum.weather

import android.app.NotificationChannel
import android.app.NotificationManager
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FirebaseMessagingServices : FirebaseMessagingService() {
    @RequiresApi(Build.VERSION_CODES.O)
    fun createNotificaion(title : String, body : String){
        val notificationChannel =
            NotificationChannel("ChannelName", "name", NotificationManager.IMPORTANCE_DEFAULT)
        val notification = NotificationCompat.Builder(baseContext)
            .setSmallIcon(R.drawable.day)
            .setContentText(title)
            .setContentText(body)
        notification.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))

        val notificationManager  = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(notificationChannel)
        notificationManager.notify(1, notification.build())


    }



    @RequiresApi(Build.VERSION_CODES.O)
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        createNotificaion(title = message.notification?.title.toString(), body = message.notification?.body.toString())
        Log.d("FirebaseMessagingService", message.notification?.body.toString())

        
    }
}