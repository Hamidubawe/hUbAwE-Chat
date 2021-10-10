package com.arewatechacademy.myapplication.Notifications

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.arewatechacademy.myapplication.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlin.random.Random

private const val CHANNEL_ID = "my_channel"

class FirebaseService : FirebaseMessagingService() {
    private lateinit var intent: Intent
    private lateinit var notification: Notification
    companion object{
        var token: String? = null
    }


    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        when {
            message.data["type"] == "request" -> {

                intent = Intent(this, ProfileActivity::class.java)
                intent.putExtra("user_id", message.data["userId"])

            }

            message.data["type"] == "chat" -> {

                intent = Intent(this, ChatActivity::class.java)
                intent.putExtra("user_id", message.data["userId"])

            }

            message.data["type"] == "comment" -> {

                intent = Intent(this, PostActivity::class.java)
                intent.putExtra("postId", message.data["userId"])

            }

            else -> {
                intent = Intent(this, MainActivity::class.java)
            }
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)



        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationID = Random.nextInt()


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotification(notificationManager)
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            intent, PendingIntent.FLAG_ONE_SHOT
        )

        when {
            message.data["type"] == "request" -> {

                notification = NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle(message.data["sender"])
                    .setContentText(message.data["message"])
                    .setSmallIcon(R.drawable.ic_person_add)
                    .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.ic_person))
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .addAction(android.R.drawable.ic_menu_view, "View Profile", pendingIntent )
                    .build()

            }
            message.data["type"] == "chat" -> {

                notification = NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle(message.data["sender"])
                    //.setContentText(message.data["message"])
                    .setSmallIcon(R.drawable.ic_chat_icon)
                    .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.ic_chat_icon))
                    .setAutoCancel(true)
                    .setStyle(NotificationCompat.BigTextStyle().bigText(message.data["message"]))
                    .setContentIntent(pendingIntent)
                    .addAction(android.R.drawable.ic_menu_view, "Reply", pendingIntent )
                    .build()

            }
            message.data["type"] == "comment" -> {

                notification = NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle(message.data["sender"])
                    .setContentText(message.data["message"])
                    .setSmallIcon(R.drawable.ic_chat_icon)
                    .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.ic_chat_icon))
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .build()

            }
            else ->{
                notification = NotificationCompat.Builder(this, CHANNEL_ID)
                    //.setContentTitle(message.data["sender"])
                    .setContentText(message.data["message"])
                    .setSmallIcon(R.drawable.ic_chat_icon)
                    .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.ic_chat_icon))
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .build()
            }
        }

        notificationManager.notify(notificationID, notification)
    }

    override fun onNewToken(p0: String) {
        super.onNewToken(p0)

        token = p0
        if (FirebaseAuth.getInstance().currentUser != null){

            val user = FirebaseAuth.getInstance().currentUser!!.uid


            FirebaseDatabase.getInstance().reference.child("Users")
                .child(user).child("token").setValue(p0)

        }


    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotification(notificationManager: NotificationManager) {

        val channelName = "channelName"
        val channel = NotificationChannel(
            CHANNEL_ID,
            channelName,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "my channel desc"
            enableLights(true)
            //enableVibration(true)
            lightColor = Color.BLUE
        }

        notificationManager.createNotificationChannel(channel)
    }

}