package  at.apramendorfer.authenticator.common.domain


data class ResolveRequest (
    val requestId: String,
    val signedData: String
)