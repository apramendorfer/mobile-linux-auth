package at.apramendorfer.authenticator.service

import at.apramendorfer.authenticator.config.Constants
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private val okHttpClient = OkHttpClient.Builder()
        .build()

    private  val gson = GsonBuilder()
        .setLenient()
        .create()

    val authRequestService: AuthenticationRequestApiService by lazy {
        Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(AuthenticationRequestApiService::class.java)
    }
}