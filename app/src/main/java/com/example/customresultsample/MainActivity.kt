package com.example.customresultsample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

class GitHubModel(val name: String)
data class GitHubEntity(val name: String)
interface GitHubClient{
    @GET("users/tatsuya-ss")
    fun fetchUser(): retrofit2.Call<GitHubEntity>
}

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        GlobalScope.launch {
            println("開始")
            launch {
                fetchGitHubUser()
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
                .client(client)
                .build()
        val githubClient = retrofit.create(GitHubClient::class.java)
        return githubClient
    }

    private fun fetchGitHubUser() {
        createService().fetchUser().enqueue(object : Callback<GitHubEntity> {
            override fun onResponse(call: Call<GitHubEntity>, response: Response<GitHubEntity>) {
                println(response.body().toString())
            }

            override fun onFailure(call: Call<GitHubEntity>, t: Throwable) {
                println(t.message)
            }

        })
    }

}