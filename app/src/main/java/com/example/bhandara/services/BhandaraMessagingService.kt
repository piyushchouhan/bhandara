package com.example.bhandara.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.bhandara.MainActivity
import com.example.bhandara.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class BhandaraMessagingService : FirebaseMessagingService() {
    
    companion object {
        private const val TAG = "BhandaraMessaging"
        private const val CHANNEL_ID = "bhandara_notifications"
        private const val CHANNEL_NAME = "Bhandara Notifications"
    }
    
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM token: $token")
        
        // TODO: Save token to Firestore when user is authenticated
        // This will be handled in MainActivity after anonymous auth
    }
    
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        
        Log.d(TAG, "Message received from: ${message.from}")
        
        // Extract data payload
        val data = message.data
        val notificationType = data["type"] ?: ""
        val feastId = data["feastId"] ?: ""
        val organizerName = data["organizerName"] ?: ""
        val address = data["address"] ?: ""
        
        Log.d(TAG, "Notification type: $notificationType")
        Log.d(TAG, "Feast ID: $feastId")
        Log.d(TAG, "Organizer: $organizerName")
        
        // Get title and body - prefer notification payload, fallback to data
        val title = message.notification?.title ?: data["title"] ?: "New Bhandara"
        val body = message.notification?.body ?: data["body"] ?: "Check out a new bhandara near you"
        
        // Show notification in system tray
        showNotification(title, body, notificationType, feastId)
    }
    
    private fun showNotification(
        title: String, 
        body: String, 
        type: String = "",
        feastId: String = ""
    ) {
        createNotificationChannel()
        
        // Intent to open MainActivity when notification is tapped
        // Later you can add feastId as extras to navigate to specific bhandara
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("notification_type", type)
            putExtra("feast_id", feastId)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this, 
            System.currentTimeMillis().toInt(), 
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(R.drawable.ic_notification)  // White notification icon
            .setAutoCancel(true)  // Dismiss when tapped
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)  // Sound, vibrate, lights
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))  // Expandable text
            .build()
        
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for nearby Bhandara events"
            }
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
