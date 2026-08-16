package com.ibnalsham.app.push

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.ibnalsham.app.MainActivity
import com.ibnalsham.app.data.notification.NotificationRepository
import com.ibnalsham.app.feature.navigation.DeepLink
import com.ibnalsham.app.library.data.cache.UserPreferencesRepository
import com.ibnalsham.app.ui.design.designSystem.R as DesignR
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

/**
 * Receives order and promotion pushes from the restaurant's backend and turns
 * them into system notifications that deep-link into the matching screen.
 */
class IbnAlshamMessagingService : FirebaseMessagingService() {

    private val notificationRepository: NotificationRepository by inject()
    private val userPreferences: UserPreferencesRepository by inject()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        registerToken(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val title = message.notification?.title
            ?: message.data["title"]
            ?: getString(DesignR.string.notifications_title)
        val body = message.notification?.body ?: message.data["body"].orEmpty()

        showNotification(title, body, message.data)
    }

    private fun registerToken(token: String) {
        // Anonymous devices have nothing to attach the token to yet; the token is
        // re-sent after sign-in by [PushTokenSync].
        if (userPreferences.getAuthToken().isNullOrBlank()) return

        scope.launch { notificationRepository.registerDeviceToken(token) }
    }

    private fun showNotification(title: String, body: String, data: Map<String, String>) {
        ensureChannel()

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            deepLinkFor(data)?.let { this.data = Uri.parse(it) }
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        if (!hasPostPermission()) return

        NotificationManagerCompat.from(this).notify(System.currentTimeMillis().toInt(), notification)
    }

    /** Map the push payload to an in-app link so tapping lands on the right screen. */
    private fun deepLinkFor(data: Map<String, String>): String? {
        data["order_id"]?.toIntOrNull()?.let { return DeepLink.orderUrl(it) }
        data["product_id"]?.toIntOrNull()?.let { return DeepLink.productUrl(it) }
        return data["url"]
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (manager.getNotificationChannel(CHANNEL_ID) != null) return

        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                getString(DesignR.string.notifications_title),
                NotificationManager.IMPORTANCE_HIGH,
            )
        )
    }

    private fun hasPostPermission(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED

    companion object {
        const val CHANNEL_ID = "ibnalsham_orders"
    }
}
