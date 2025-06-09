package at.apramendorfer.authenticator.repositories

import at.apramendorfer.authenticator.common.domain.AuthenticationRequest
import at.apramendorfer.authenticator.common.domain.ResolveRequest
import at.apramendorfer.authenticator.config.Constants
import at.apramendorfer.authenticator.service.RetrofitClient
import at.apramendorfer.authenticator.service.SnackbarEvent
import at.apramendorfer.authenticator.service.SnackbarService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

object AuthRequestRepository {
    private val _requests = MutableStateFlow<List<AuthenticationRequest>>(emptyList())
    private val _isLoading = MutableStateFlow(false)

    val requests: Flow<List<AuthenticationRequest>> get() = _requests
    val isLoading: Flow<Boolean> = _isLoading

    suspend fun loadRequests() {
        _isLoading.emit(true);
        try {
            val result = RetrofitClient.authRequestService.getAuthenticationRequests(Constants.USERNAME)
            _requests.emit(result)
            _isLoading.emit(false)
        } catch (e: Exception) {
            SnackbarService.sendEvent(SnackbarEvent("Failed to load requests."))
            _isLoading.emit(false)
        }
    }

    fun getRequestById(requestId: String): AuthenticationRequest? {
        return _requests.value.firstOrNull { it.uuid == requestId }
    }

    suspend  fun resolveRequest(requestId: String, signedData: String): Boolean {
        val result = RetrofitClient.authRequestService.postRequestData(ResolveRequest(requestId, signedData))
        if(result.isSuccessful) {
            _requests.value = _requests.value.filter { it.uuid != requestId }
            SnackbarService.sendEvent(SnackbarEvent("Request has been resolved."))
        } else {
            SnackbarService.sendEvent(SnackbarEvent("Request could not be resolved."))
        }

        return result.isSuccessful
    }
}