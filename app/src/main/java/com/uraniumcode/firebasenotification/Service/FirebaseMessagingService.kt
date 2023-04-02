package com.uraniumcode.firebasenotification.Service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.uraniumcode.firebasenotification.MainActivity
import com.uraniumcode.firebasenotification.R
import java.io.IOException

class FirebaseMessagingService : FirebaseMessagingService() {

    private val CHANNEL_ID = "FcmChannel"
    private val CHANNEL_NAME = "FirebaseNotification"
    private val NOTIFICATION_ID = 22
    private val REQ_CODE = 111

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        remoteMessage.data.let {
            val imageUrlList = remoteMessage.data["images_url"]?.split(",") ?: listOf()
            setNotificationSliderData(it["title"]!!, it["body"]!!,imageUrlList)
        }
    }

    private fun sendNotification(title: String, body: String, remoteViews: RemoteViews) {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            this, REQ_CODE, intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setSmallIcon(androidx.core.R.drawable.notification_icon_background)
            .setContentIntent(pendingIntent)
            .setCustomBigContentView(remoteViews)

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
    }

    private fun setNotificationSliderData(title: String, notificationMessage: String, imageList: List<String>) {
        val remoteViews = RemoteViews(packageName, R.layout.notification_slider_layout)
        remoteViews.setTextViewText(R.id.tv_title, title)
        remoteViews.setTextViewText(R.id.tv_body, notificationMessage)
        for (imgUrl: String in imageList) {
            val viewFlipperImage = RemoteViews(packageName, R.layout.notification_slider_image)
            try {
                Glide.with(applicationContext)
                    .asBitmap()
                    .load(imgUrl)
                    .into(object : SimpleTarget<Bitmap>() {
                        override fun onResourceReady(
                            resource: Bitmap,
                            transition: Transition<in Bitmap>?)
                        {
                            viewFlipperImage.setImageViewBitmap(R.id.imageView, resource)
                            remoteViews.addView(R.id.viewFlipper, viewFlipperImage)
                            sendNotification(notificationMessage, title, remoteViews)
                        }
                    })
            } catch (e: IOException) {
                println(e.message)
            }
        }
    }
}