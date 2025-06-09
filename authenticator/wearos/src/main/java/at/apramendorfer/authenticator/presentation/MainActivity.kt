/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter and
 * https://github.com/android/wear-os-samples/tree/main/ComposeAdvanced to find the most up to date
 * changes to the libraries and their usages.
 */

package at.apramendorfer.authenticator.presentation

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material3.Icon
import at.apramendorfer.authenticator.R
import at.apramendorfer.authenticator.common.Constants.EXTRA_BIOMETRIC_SETTINGS
import at.apramendorfer.authenticator.common.Constants.EXTRA_CHALLENGE
import at.apramendorfer.authenticator.common.Constants.EXTRA_OTP
import at.apramendorfer.authenticator.common.Constants.EXTRA_REQUEST_ID
import at.apramendorfer.authenticator.presentation.theme.AuthenticatorTheme
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.Wearable

class MainActivity : ComponentActivity() {
    private val myViewModel: MainViewModel by viewModels()

    private val dataClient by lazy { Wearable.getDataClient(this) }
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)
        checkForRequestId(intent)
        setTheme(android.R.style.Theme_DeviceDefault)
        setContent {
            WearApp(dataClient)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        checkForRequestId(intent)
    }

    private fun checkForRequestId(intent: Intent) {
        val authId = intent.getStringExtra(EXTRA_REQUEST_ID)
        val challenge = intent.getStringExtra(EXTRA_CHALLENGE)
        val otp = intent.getStringExtra(EXTRA_OTP)
        val biometrics = intent.getBooleanExtra(EXTRA_BIOMETRIC_SETTINGS, false)
        myViewModel.setBiometric(biometrics)
        myViewModel.setOtp(otp)

        if (!authId.isNullOrEmpty() && !challenge.isNullOrEmpty()) {
            myViewModel.selectRequest(authId, challenge)
        }
    }

    override fun onResume() {
        super.onResume()
        dataClient.addListener(myViewModel)
    }

    override fun onPause() {
        super.onPause()
        dataClient.removeListener(myViewModel)
    }

}

@Composable
fun WearApp(dataClient: DataClient, vm: MainViewModel = viewModel()) {
    val state by vm.uiState.collectAsState();
    AuthenticatorTheme {
        Scaffold(
            timeText = {
                TimeText()
            }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                if (state.selectedRequestId != null) {
                    if (state.fingerPrintMode) {
                        FingerprintButton(onClick = {
                            vm.resolveRequest(dataClient, state.selectedRequestId!!)
                        }, otp = state.otp ?: "")
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "Do you approve?",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = "[${state.otp}]",
                                fontSize = 20.sp,
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Button(onClick = { vm.clearRequest() }) {
                                    Icon(
                                        painter = painterResource(R.drawable.close24),
                                        contentDescription = "decline",
                                        modifier = Modifier
                                            .size(ButtonDefaults.DefaultIconSize)
                                            .wrapContentSize(align = Alignment.Center)
                                    )
                                }
                                Button(onClick = {
                                    vm.resolveRequest(
                                        dataClient,
                                        state.selectedRequestId!!
                                    )
                                }) {
                                    Icon(
                                        painter = painterResource(R.drawable.check24),
                                        contentDescription = "approve",
                                        modifier = Modifier
                                            .size(ButtonDefaults.DefaultIconSize)
                                            .wrapContentSize(align = Alignment.Center)
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Text("No request")
                }
            }
        }
    }
}

@Composable
fun FingerprintButton(
    onClick: () -> Unit,
    otp: String
) {
    val infiniteTransition = rememberInfiniteTransition(label = "")
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = ""
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("[${otp}]")
        Box(
            modifier = Modifier
                .size(100.dp) // Adjusted size for wearables
                .graphicsLayer {
                    scaleX = glowScale
                    scaleY = glowScale
                }
                .clip(CircleShape)
                .background(Color(0xFF3A3A3A).copy(alpha = 0.8f))
                .combinedClickable(
                    onClick = {},
                    onLongClick = {
                        onClick()
                    }),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(70.dp) // Inner button size, slightly smaller than outer circle
                    .clip(CircleShape)
                    .background(Color(0xFF4CAF50)),
                contentAlignment = Alignment.Center // Center content within the Box
            ) {
                Icon(
                    painter = painterResource(R.drawable.fingerprint),
                    contentDescription = "confirm",
                    modifier = Modifier.size(ButtonDefaults.LargeButtonSize)
                )
            }
        }
    }
}



@Preview(
    showBackground = true
)
@Composable
fun FingerprintButtonPreview() {
    FingerprintButton(
        onClick = {},
        otp = "123456"
    )
}
