package at.apramendorfer.authenticator.services

import android.content.Intent
import at.apramendorfer.authenticator.common.Constants.AUTH_REQUEST_PATH
import at.apramendorfer.authenticator.common.Constants.EXTRA_BIOMETRIC_SETTINGS
import at.apramendorfer.authenticator.common.Constants.EXTRA_CHALLENGE
import at.apramendorfer.authenticator.common.Constants.EXTRA_OTP
import at.apramendorfer.authenticator.common.Constants.EXTRA_REQUEST_ID
import at.apramendorfer.authenticator.presentation.MainActivity
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService

class DataLayerService: WearableListenerService() {
    override fun onMessageReceived(messageEvent: MessageEvent) {

    }

    override fun onDataChanged(p0: DataEventBuffer) {
        p0.forEach {
            if (it.type == DataEvent.TYPE_CHANGED) {
                if (it.dataItem.uri.path == AUTH_REQUEST_PATH) {
                    val dataMap = DataMapItem.fromDataItem(it.dataItem).dataMap
                    val id = dataMap.getString(EXTRA_REQUEST_ID)
                    val challenge = dataMap.getString(EXTRA_CHALLENGE)
                    val otp = dataMap.getString(EXTRA_OTP)
                    val biometrics = dataMap.getBoolean(EXTRA_BIOMETRIC_SETTINGS)
                    if (id != null && challenge!= null) {
                        val i = Intent(this, MainActivity::class.java)
                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        i.putExtra(EXTRA_REQUEST_ID, id)
                        i.putExtra(EXTRA_CHALLENGE, challenge)
                        i.putExtra(EXTRA_OTP, otp)
                        i.putExtra(EXTRA_BIOMETRIC_SETTINGS, biometrics)
                        i.setAction("")
                        startActivity(i)
                    }
                }
            }
        }
    }
}