package at.apramendorfer.authenticator.common

object Constants {
    // Wear Data API paths (make sure to check manifest of applications if you change these paths)
    const val AUTH_REQUEST_PATH = "/auth-request"
    const val AUTH_REQUEST_RESOLVED_PATH = "/auth-request-resolved"


    // Intent and bundle extras
    const val EXTRA_REQUEST_ID = "requestId"
    const val EXTRA_CHALLENGE = "challenge"
    const val EXTRA_BIOMETRIC_SETTINGS = "biometric"
    const val EXTRA_OTP = "otp"
}