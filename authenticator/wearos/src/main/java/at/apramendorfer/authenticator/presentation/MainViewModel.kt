package at.apramendorfer.authenticator.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import at.apramendorfer.authenticator.common.Constants
import at.apramendorfer.authenticator.common.Constants.AUTH_REQUEST_PATH
import at.apramendorfer.authenticator.common.Constants.EXTRA_CHALLENGE
import at.apramendorfer.authenticator.common.Constants.EXTRA_REQUEST_ID
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.CapabilityInfo
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.PutDataRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class State(
    val selectedRequestId: String? = null,
    val challenge: String? = null,
    val otp: String? = null,
    val fingerPrintMode: Boolean = true
)


class MainViewModel : ViewModel(), DataClient.OnDataChangedListener,
    CapabilityClient.OnCapabilityChangedListener {
    private val _uiState = MutableStateFlow(State())
    val uiState: StateFlow<State> = _uiState.asStateFlow()

    fun resolveRequest(dataClient: DataClient, requestId: String) {
        val request: PutDataRequest =
            PutDataMapRequest.create(Constants.AUTH_REQUEST_RESOLVED_PATH).run {
                dataMap.putString(EXTRA_REQUEST_ID, requestId)
                dataMap.putString(EXTRA_CHALLENGE, _uiState.value.challenge ?: "")
                asPutDataRequest()
            }
        dataClient.putDataItem(request).addOnSuccessListener {
            clearRequest()
        }
            .addOnFailureListener { e ->
                Log.e("WearOS", "Failed to send data", e)
            }
    }


    fun selectRequest(id: String, challenge: String) {
        _uiState.update { state ->
            state.copy(
                selectedRequestId = id,
                challenge = challenge
            )
        }
    }

    fun clearRequest() {
        _uiState.update { state ->
            state.copy(
                selectedRequestId = null,
                challenge = null
            )
        }
    }

    override fun onCapabilityChanged(p0: CapabilityInfo) {
        TODO("Not yet implemented")
    }

    override fun onDataChanged(p0: DataEventBuffer) {
        p0.forEach {
            if (it.type == DataEvent.TYPE_CHANGED) {
                if (it.dataItem.uri.path == AUTH_REQUEST_PATH) {
                    val dataMap = DataMapItem.fromDataItem(it.dataItem).dataMap
                    val id = dataMap.getString(EXTRA_REQUEST_ID)
                    val challenge = dataMap.getString(EXTRA_CHALLENGE)

                    if (id != null && challenge != null) {
                        selectRequest(id, challenge)
                    }
                }
            }
        }
    }

    fun setBiometric(value: Boolean) {
        _uiState.update { state ->
            state.copy(
                fingerPrintMode = value
            )
        }
    }

    fun setOtp(value: String?) {
        _uiState.update { state ->
            state.copy(
                otp = value
            )
        }
    }
}