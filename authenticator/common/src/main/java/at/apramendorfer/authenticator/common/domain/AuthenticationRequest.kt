package at.apramendorfer.authenticator.common.domain

import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date


data class AuthenticationRequest(
    val challenge: String,
    val uuid: String,
    val otp: String,
    val shortName: String,
    val timestamp: Date,
    val expiresAt: Date
) {
    fun isExpiresInS(): Long {
        val date = expiresAt.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
        return Duration.between(LocalDateTime.now(), date).seconds
   }
}