package com.arewatechacademy.myapplication.Notifications

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.arewatechacademy.myapplication.R
import kotlin.random.Random

class ReminderBroadcast : BroadcastReceiver() {
   private val CHANNEL_ID = "channel"
    override fun onReceive(context: Context?, intent: Intent?) {

        //val notificationManager = NotificationManagerCompat.from(context!!)

        val notificationManager
                = context!!.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notificationID = Random.nextInt()



        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotification(notificationManager)
        }

       val notification: Notification? = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("New Post Alert")
            .setContentText("Check out new trending post")
            .setSmallIcon(R.drawable.ic_chat_icon)
            .setAutoCancel(true)
            //.setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(notificationID, notification)


        val receiver = ComponentName(context, ReminderBroadcast::class.java)
        val pm: PackageManager = context.packageManager
        pm.setComponentEnabledSetting(receiver, PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
        PackageManager.DONT_KILL_APP)

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
            enableVibration(true)
            lightColor = Color.BLUE
        }

        notificationManager.createNotificationChannel(channel)
    }

}