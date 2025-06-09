package at.apramendorfer.authenticator

import android.content.Intent
import android.content.SharedPreferences
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import at.apramendorfer.authenticator.common.domain.AuthenticationRequest
import at.apramendorfer.authenticator.service.BiometricPromptManager
import at.apramendorfer.authenticator.service.SnackbarService
import kotlinx.coroutines.launch
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun AuthenticatorHomeScreen(
    promptManager: BiometricPromptManager,
    goToSettings: () -> Unit,
    sharedPreferences: SharedPreferences,
    vm: AuthRequestViewModel
) {
    Surface(
        modifier = Modifier.fillMaxSize()
    ) {
        AuthenticatorBaseLayout(goToSettings, promptManager, sharedPreferences, vm)
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(goToSettings: () -> Unit) {
    TopAppBar(
        title = { Text("Authenticator") },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.primary
        ),
        actions = {
            IconButton(onClick = { goToSettings() }) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings"
                )
            }
        }
    )
}


@Composable
fun RequestList(
    modifier: Modifier,
    requests: List<AuthenticationRequest>,
    vm: AuthRequestViewModel
) {
    if (requests.isEmpty()) {
        EmptyState()
    } else {
        LazyColumn(modifier) {
            items(requests) { request ->
                RequestItem(request) { id -> vm.selectRequest(id, request.otp) }
                HorizontalDivider()
            }
        }
    }
}

@Composable
fun EmptyState() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .wrapContentSize(Alignment.Center) // Center the empty state message
    ) {
        Text(
            text = "No requests available",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun AuthenticatorBaseLayout(
    goToSettings: () -> Unit,
    promptManager: BiometricPromptManager,
    sharedPreferences: SharedPreferences,
    vm: AuthRequestViewModel
) {
    val snackbarHostState = remember {
        SnackbarHostState()
    }
    val requests by vm.requests.observeAsState(initial = emptyList())
    val isLoading by vm.isLoading.observeAsState(initial = true)

    val state by vm.uiState.collectAsState();

    val currentState = rememberUpdatedState(state)

    val scope = rememberCoroutineScope()
    ObserveAsEvents(flow = SnackbarService.events, snackbarHostState) { event ->
        scope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()

            val result = snackbarHostState.showSnackbar(
                message = event.message,
                actionLabel = event.action?.name,
                duration = SnackbarDuration.Long
            )

            if (result == SnackbarResult.Dismissed) {
                event.action?.action?.invoke()
            }
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        topBar = { TopBar(goToSettings) }
    )
    { paddingValues ->
        Box(Modifier.padding(paddingValues)) {
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f))
                        .wrapContentSize(Alignment.Center)  // Center the spinner
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            RequestList(Modifier, requests, vm)
        }
    }
    val biometricResult by promptManager.promptResult.collectAsState(initial = null)
    val enrollLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = {
            println("Activity result: $it")
        }
    )
    LaunchedEffect(biometricResult) {
        if (biometricResult is BiometricPromptManager.BiometricResult.AuthenticationNotSet) {
            val enrollIntent = Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
                putExtra(
                    Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                    BIOMETRIC_STRONG
                )
            }
            enrollLauncher.launch(enrollIntent)
        }

        biometricResult?.let { result ->
            when (result) {
                is BiometricPromptManager.BiometricResult.AuthenticationError -> {
                    // Handle authentication error
                    vm.clearRequest()
                }

                BiometricPromptManager.BiometricResult.AuthenticationFailed -> {
                    // Handle authentication failure
                    vm.clearRequest()
                }

                BiometricPromptManager.BiometricResult.AuthenticationNotSet -> {
                    // Handle authentication not set
                    vm.clearRequest()
                }

                is BiometricPromptManager.BiometricResult.AuthenticationSuccess -> {
                    // Handle successful authentication
                    //vm.viewModelScope.launch {
                        vm.resolveRequest(result.requestId)
                   // }
                }

                BiometricPromptManager.BiometricResult.FeatureUnavailable -> {
                    // Handle feature unavailable
                    vm.clearRequest()
                }

                BiometricPromptManager.BiometricResult.HardwareUnavailable -> {
                    // Handle hardware unavailable
                    vm.clearRequest()
                }
            }
        }
    }

    LaunchedEffect(state) {
        if (sharedPreferences.getBoolean("biometric", false) && state.selectedRequestId != null) {
            val otp = state.otp
            promptManager.showBiometricPrompt(
                title = "Confirm Request [$otp]",
                description = "Are you sure you want to confirm this request?",
                state.selectedRequestId!!
            )
            vm.clearRequest()
        }
    }

    when {
        currentState.value.selectedRequestId != null && currentState.value.otp != null -> {
            if (!sharedPreferences.getBoolean("biometric", false)) {
                ConfirmDialog(otp = state.otp!!, onConfirmation = {
                    vm.viewModelScope.launch {
                        vm.resolveRequest(state.selectedRequestId!!)
                    }
                }, onDismissRequest = {
                    vm.clearRequest()
                })
            }
        }
    }
}

@Composable
fun ConfirmDialog(otp: String, onDismissRequest: () -> Unit, onConfirmation: () -> Unit) {
    AlertDialog(
        title = {
            Text(text = "Confirm Request [$otp]")
        },
        text = {
            Text(text = "Are you sure you want to confirm this request?")
        },
        onDismissRequest = {
            onDismissRequest()
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirmation()
                }
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismissRequest()
                }
            ) {
                Text("Dismiss")
            }
        }
    )
}


@Composable
fun RequestItem(
    request: AuthenticationRequest,
    onClickAction: (requestId: String) -> Unit
) {
    val formatter = DateTimeFormatter.ofPattern("HH:mm:ss")
    val date = request.expiresAt.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()

    Column(modifier = Modifier
        .clickable {
            onClickAction(request.uuid)
        }
        .fillMaxWidth()) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(text = request.otp, fontWeight = FontWeight.Bold)
            Text(text = formatter.format(date))
        }
    }
}
