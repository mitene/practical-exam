package us.mitene.practicalexam.network

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.http.GET
import javax.inject.Inject
import javax.inject.Singleton

interface GithubApi {
    @GET("orgs/mixigroup/repos")
    suspend fun getRepos(): List<GithubRepo>
}

@Singleton
class GithubRepoRemoteDataSource @Inject constructor() {
    private val json = Json { ignoreUnknownKeys = true }
    private val retrofit = Retrofit.Builder()
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .baseUrl(BASE_URL)
        .build()
    private val retrofitService: GithubApi by lazy {
        retrofit.create(GithubApi::class.java)
    }

    suspend fun getRepos(): List<GithubRepo> = retrofitService.getRepos()

    companion object {
        private const val BASE_URL = "https://api.github.com"
    }
}