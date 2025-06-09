package at.apramendorfer.authenticator.service

import at.apramendorfer.authenticator.common.domain.AuthenticationRequest
import at.apramendorfer.authenticator.common.domain.ResolveRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface AuthenticationRequestApiService {

    @GET("/Authentication")
    suspend fun getAuthenticationRequests(@Query("userName") userName: String): List<AuthenticationRequest>

    @POST("/Authentication")
    suspend fun postRequestData(@Body requestData: ResolveRequest): Response<Void>
}