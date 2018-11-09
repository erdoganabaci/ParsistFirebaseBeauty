package com.example.erdo.parsistapps


import android.media.RingtoneManager
import android.support.v4.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService:FirebaseMessagingService(){
    override fun onMessageReceived(p0: RemoteMessage?) {
        val sounduri=RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val builder=NotificationCompat.Builder(this)
        builder.setSmallIcon(R.drawable.ic_notification)
        builder.setSound(sounduri)
    }

}