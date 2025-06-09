package at.apramendorfer.authenticator.service

import at.apramendorfer.authenticator.common.Constants
import at.apramendorfer.authenticator.repositories.AuthRequestRepository
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Base64

class DataClientService : WearableListenerService() {
    private val _cryptoManager = CryptoManagerService();
    override fun onDataChanged(dataEvents: DataEventBuffer) {
        dataEvents.forEach {
            if (it.type == DataEvent.TYPE_CHANGED) {
                if (it.dataItem.uri.path == Constants.AUTH_REQUEST_RESOLVED_PATH) {
                    val dataMap = DataMapItem.fromDataItem(it.dataItem).dataMap
                    val id = dataMap.getString(Constants.EXTRA_REQUEST_ID)
                    val challenge = dataMap.getString(Constants.EXTRA_CHALLENGE)
                    if (id != null) {
                        CoroutineScope(Dispatchers.IO).launch {
                            val c = challenge ?: ""
                            val base64 = Base64.getEncoder()
                                .encodeToString(_cryptoManager.decryptWithDefaultKey(c))
                            AuthRequestRepository.resolveRequest(id, base64)
                        }
                    }
                }
            }
        }

    }
}