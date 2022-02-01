package com.example.customresultsample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.gson.annotations.SerializedName
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import java.io.Serializable
import java.lang.Exception

class GitHubModel(val name: String)

data class GitHubEntity(
        @SerializedName("name")
        val name: String
        )

interface GitHubClient{
    @GET("users/tatsuya-ss")
    suspend fun fetchUser(): GitHubEntity
}

sealed class Result<out R> {
    // outをつけると、Tは戻り値の型としてしか使わない
    data class Success<out T>(val data: T): Result<T>()
    data class Failure(val exception: Exception): Result<Nothing>()
}

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        GlobalScope.launch {
            println("開始")
            launch {
                val result = fetchGitHubUser()
                when(result) {
                    is Result.Success -> { println(result.data.name) }
                    is Result.Failure -> { println(result.exception.message) }
                }
            }
        }

    }

    private fun makeOkHttp(): OkHttpClient.Builder {
        val httpClient = OkHttpClient.Builder()
        httpClient.addInterceptor { chain ->
            val request = chain.request().newBuilder().build()
            val response = chain.proceed(request)
            return@addInterceptor response
        }
        return httpClient
    }

    private fun createService(): GitHubClient {
        val client = makeOkHttp().build()
        val retrofit = Retrofit.Builder()
                .baseUrl("https://api.github.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(CoroutineCallAdapterFactory())
                .client(client)
                .build()
        val githubClient = retrofit.create(GitHubClient::class.java)
        return githubClient
    }

    private suspend fun fetchGitHubUser(): Result<GitHubEntity> {
        println("fetchGitHubUser")
        return try {
            val result = createService().fetchUser()
            Result.Success(result)
        } catch (e: Exception) {
            println(e.message)
            Result.Failure(e)
        }
    }

}