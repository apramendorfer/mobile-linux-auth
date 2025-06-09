package at.apramendorfer.authenticator

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import at.apramendorfer.authenticator.common.domain.AuthenticationRequest
import at.apramendorfer.authenticator.repositories.AuthRequestRepository
import at.apramendorfer.authenticator.service.CryptoManagerService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Base64
import java.util.Date

data class RequestListState(
    val selectedRequestId: String? = null,
    val otp: String? = null,
)

class AuthRequestViewModel : ViewModel() {
    private val _cryptoManager = CryptoManagerService();

    private val _uiState = MutableStateFlow(RequestListState())
    val uiState: StateFlow<RequestListState> = _uiState.asStateFlow()


    private val _requests = MutableLiveData<List<AuthenticationRequest>>(emptyList())
    val requests: LiveData<List<AuthenticationRequest>> = _requests
    private var _storedData = emptyList<AuthenticationRequest>()

    val isLoading = AuthRequestRepository.isLoading.asLiveData();

    init {
        viewModelScope.launch {
            AuthRequestRepository.requests.collect { data ->
                _storedData = data
            }
        }

        viewModelScope.launch {
            AuthRequestRepository.loadRequests()

            while (isActive) {
                val filtered = _storedData.filter { it.expiresAt.after(Date()) }
                if (
                    isLoading.value == false &&
                    uiState.value.selectedRequestId != null &&
                    filtered.find { it.uuid == uiState.value.selectedRequestId } == null
                ) {
                    clearRequest()
                }
                _requests.postValue(filtered)
                delay(150)
            }
        }
    }

    suspend fun resolveRequest(requestId: String) {
        val request = AuthRequestRepository.getRequestById(requestId);
        clearRequest()
        if (request != null) {
            val base64 = Base64.getEncoder()
                .encodeToString(_cryptoManager.decryptWithDefaultKey(request.challenge))
            AuthRequestRepository.resolveRequest(requestId, base64)
        }
    }

    fun selectRequest(id: String, otp: String) {
        _uiState.update { state ->
            state.copy(
                selectedRequestId = id,
                otp = otp
            )
        }
    }

    fun clearRequest() {
        _uiState.update { state ->
            state.copy(
                selectedRequestId = null,
                otp = null
            )
        }
    }
}