package at.apramendorfer.authenticator

import android.content.SharedPreferences
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import at.apramendorfer.authenticator.service.CryptoManagerService

@Composable
fun SettingsView(
    goBack: () -> Unit,
    vm: AuthRequestViewModel = viewModel(),
    sharedPreferences: SharedPreferences
) {
    val clipboardManager = LocalClipboardManager.current
    val cryptoManager = CryptoManagerService()
    val publicKey = cryptoManager.getPublicKeyAsPem()

    var isChecked by remember { mutableStateOf(sharedPreferences.getBoolean("biometric", false)) }
    Surface(
        modifier = Modifier.fillMaxSize(),
    ) {
        Scaffold(
            topBar = { SettingsTopBar(goBack) }
        )
        { paddingValues ->
            Box(Modifier.padding(paddingValues)) {
                Column {
                    Text(publicKey)
                    Button(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(publicKey))
                        }
                    ) {
                        Text("Click to copy key to clipboard")
                    }
                    Text(
                        text = if (isChecked) "Biometrics ON" else "Biometrics OFF",
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Switch(
                        checked = isChecked,
                        onCheckedChange = {
                            isChecked = it
                            sharedPreferences.edit().putBoolean("biometric", isChecked).apply()
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsTopBar(goBack: () -> Unit) {
    TopAppBar(
        title = { Text("Settings") },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.primary
        ),
        navigationIcon = {
            IconButton(onClick = goBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
        },
    )
}

