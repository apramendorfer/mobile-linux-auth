package at.apramendorfer.authenticator

import android.content.Context
import android.content.Intent

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.compose.rememberNavController
import at.apramendorfer.authenticator.common.Constants.EXTRA_OTP
import at.apramendorfer.authenticator.common.Constants.EXTRA_REQUEST_ID
import at.apramendorfer.authenticator.service.BiometricPromptManager
import at.apramendorfer.authenticator.ui.theme.AppTheme

class MainActivity : AppCompatActivity() {

    private val promptManager by lazy {
        BiometricPromptManager(this)
    }

    private val myViewModel: AuthRequestViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val sharedPreferences =
            getSharedPreferences("at.apramendorfer.authenticator", Context.MODE_PRIVATE)
        checkForRequestId(intent)
        setContent {
            AppTheme {
                val navController = rememberNavController()
                AuthenticatorNavHost(navController, promptManager, sharedPreferences)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        checkForRequestId(intent)
    }

    private fun checkForRequestId(intent: Intent) {
        val authId = intent.getStringExtra(EXTRA_REQUEST_ID)
        val otp = intent.getStringExtra(EXTRA_OTP)
        if (!authId.isNullOrEmpty()) myViewModel.selectRequest(authId, otp ?: "")
    }
}
