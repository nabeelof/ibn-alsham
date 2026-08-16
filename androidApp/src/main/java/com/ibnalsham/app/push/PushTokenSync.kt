package com.ibnalsham.app.push

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import com.ibnalsham.app.data.notification.NotificationRepository
import com.ibnalsham.app.library.data.cache.UserPreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Pushes the current FCM token to the backend so order notifications can reach
 * this device. Safe to call when Firebase is not configured — it no-ops.
 */
object PushTokenSync {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun syncIfSignedIn(
        context: Context,
        userPreferences: UserPreferencesRepository,
        notificationRepository: NotificationRepository,
    ) {
        if (userPreferences.getAuthToken().isNullOrBlank()) return

        // google-services.json may not be present in every build flavour.
        if (FirebaseApp.getApps(context).isEmpty()) {
            Log.i(TAG, "Firebase not configured; skipping push token registration")
            return
        }

        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token ->
                scope.launch { notificationRepository.registerDeviceToken(token) }
            }
            .addOnFailureListener { error ->
                Log.w(TAG, "Could not obtain FCM token", error)
            }
    }

    private const val TAG = "PushTokenSync"
}
