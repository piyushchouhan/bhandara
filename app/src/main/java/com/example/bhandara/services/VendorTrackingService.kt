package com.example.bhandara.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.bhandara.MainActivity
import com.example.bhandara.R

/**
 * Foreground service that keeps the vendor's cart location shared while the screen is off or the app is
 * in the background. Android throttles location and network for background apps, so without this the
 * cart would vanish from customers' maps whenever the vendor locks their phone.
 *
 * Started and stopped by the vendor-mode switch; the location work itself is done by [VendorLocationManager].
 */
class VendorTrackingService : Service() {

    companion object {
        private const val TAG = "VendorTrackingService"
        private const val CHANNEL_ID = "vendor_live"
        private const val NOTIFICATION_ID = 4201
        private const val EXTRA_SHOP_ID = "shop_id"
        private const val EXTRA_OWNER_UID = "owner_uid"

        fun start(context: Context, shopId: Long, ownerUid: String) {
            val intent = Intent(context, VendorTrackingService::class.java)
                .putExtra(EXTRA_SHOP_ID, shopId)
                .putExtra(EXTRA_OWNER_UID, ownerUid)
            ContextCompat.startForegroundService(context, intent)
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, VendorTrackingService::class.java))
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val shopId = intent?.getLongExtra(EXTRA_SHOP_ID, -1L) ?: -1L
        val ownerUid = intent?.getStringExtra(EXTRA_OWNER_UID)
        if (shopId <= 0 || ownerUid == null) {
            stopSelf()
            return START_NOT_STICKY
        }

        try {
            createNotificationChannel()
            startForeground(NOTIFICATION_ID, buildNotification(), ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION)
        } catch (e: Exception) {
            // e.g. location permission revoked; without it there is nothing to share
            Log.e(TAG, "Could not start vendor tracking in the foreground", e)
            stopSelf()
            return START_NOT_STICKY
        }

        VendorLocationManager.getInstance(this).start(shopId, ownerUid)

        // If the system kills the app, the vendor-mode switch restarts sharing next time the app opens
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        VendorLocationManager.getInstance(this).stop()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun buildNotification(): Notification {
        val openApp = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("You're live on the map")
            .setContentText("Customers nearby can see your cart. Turn off vendor mode in the app to stop.")
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setContentIntent(openApp)
            .build()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Vendor live location",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Shown while your cart's location is being shared with customers"
        }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }
}
