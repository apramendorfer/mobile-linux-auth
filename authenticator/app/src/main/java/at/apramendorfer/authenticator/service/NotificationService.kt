package at.apramendorfer.authenticator.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import androidx.core.app.NotificationCompat
import at.apramendorfer.authenticator.MainActivity
import at.apramendorfer.authenticator.R
import at.apramendorfer.authenticator.common.Constants
import at.apramendorfer.authenticator.common.Constants.EXTRA_BIOMETRIC_SETTINGS
import at.apramendorfer.authenticator.common.Constants.EXTRA_CHALLENGE
import at.apramendorfer.authenticator.common.Constants.EXTRA_OTP
import at.apramendorfer.authenticator.common.Constants.EXTRA_REQUEST_ID
import at.apramendorfer.authenticator.repositories.AuthRequestRepository
import com.google.android.gms.tasks.Task
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.PutDataRequest
import com.google.android.gms.wearable.Wearable
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.i("Token", token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)


        if (message.data["Type"] == "NEW_REQUEST") {
            CoroutineScope(Dispatchers.IO).launch {
                AuthRequestRepository.loadRequests()
            }
            forwardNotificationToWearOS(
                message.data["RequestId"].orEmpty(),
                message.data["Otp"].orEmpty(),
                message.data["Challenge"].orEmpty(),
                getSharedPreferences(
                    "at.apramendorfer.authenticator",
                    Context.MODE_PRIVATE
                ).getBoolean("biometric", false)
            )
        }
    }


    private fun forwardNotificationToWearOS(
        requestId: String, otp: String, challenge: String,
        requireBiometrics: Boolean
    ) {
        val request: PutDataRequest = PutDataMapRequest.create(Constants.AUTH_REQUEST_PATH).run {
            dataMap.putString(EXTRA_REQUEST_ID, requestId)
            dataMap.putString(EXTRA_OTP, otp)
            dataMap.putString(EXTRA_CHALLENGE, challenge)
            dataMap.putBoolean(EXTRA_BIOMETRIC_SETTINGS, requireBiometrics)
            asPutDataRequest()
        }

        Wearable.getNodeClient(this).connectedNodes
            .addOnCompleteListener { task: Task<List<Node>> ->
                if (task.isSuccessful) {
                    val nodes = task.result.filter { it.isNearby }
                    if (nodes.isEmpty()) {
                        sendNotification(
                            "New authentication request",
                            otp,
                            "Open app to approve request",
                            requestId
                        )
                    } else {
                        Wearable.getDataClient(this).putDataItem(request).addOnSuccessListener {
                            Log.d("WearOS", "Data sent successfully")
                        }
                            .addOnFailureListener { e ->
                                Log.e("WearOS", "Failed to send data", e)
                            }
                    }
                } else {
                    Log.e("Wear", "Error getting connected nodes: ${task.exception}")
                }
            }
    }

    private fun sendNotification(
        title: String,
        otp: String,
        messageBody: String,
        requestId: String,
    ) {
        val intent = Intent(this, MainActivity::class.java)

        intent.putExtra(EXTRA_REQUEST_ID, requestId)
        intent.putExtra(EXTRA_OTP, otp)
        intent.setAction("")

        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val requestCode = 0
        val pendingIntent = PendingIntent.getActivity(
            this,
            requestCode,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )


        val channelId = "fcm_default_channel"
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(messageBody)
            .setAutoCancel(true)

            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Since android Oreo notification channel is needed.
        val channel = NotificationChannel(
            channelId,
            "Android Wear OS Channel",
            NotificationManager.IMPORTANCE_HIGH,
        )

        notificationManager.createNotificationChannel(channel)
        notificationManager.notify(0, notificationBuilder.build())
    }
}