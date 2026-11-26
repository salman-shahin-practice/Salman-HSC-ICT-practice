package com.example.data.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

@JsonClass(generateAdapter = true)
data class WandboxCompileRequest(
    val code: String,
    val compiler: String = "gcc-head-c",
    val stdin: String = "",
    @Json(name = "compiler-option-raw") val compilerOptionRaw: String = "-lm"
)

@JsonClass(generateAdapter = true)
data class WandboxCompileResponse(
    val status: String? = null,
    @Json(name = "program_output") val programOutput: String? = null,
    @Json(name = "program_error") val programError: String? = null,
    @Json(name = "compiler_error") val compilerError: String? = null,
    @Json(name = "compiler_message") val compilerMessage: String? = null
)

interface WandboxApi {
    @POST("api/compile.json")
    suspend fun compileC(@Body request: WandboxCompileRequest): WandboxCompileResponse
}

object CloudCompilerClient {
    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttp: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(12, TimeUnit.SECONDS)
        .writeTimeout(8, TimeUnit.SECONDS)
        .build()

    val api: WandboxApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://wandbox.org/")
            .client(okHttp)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(WandboxApi::class.java)
    }
}
