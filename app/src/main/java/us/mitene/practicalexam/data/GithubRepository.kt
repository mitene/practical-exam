package us.mitene.practicalexam.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import com.google.gson.GsonBuilder
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import timber.log.Timber
import java.util.concurrent.TimeUnit

class GithubRepository(
    context: Context,
    private val remote: GithubService = Service.githubService,
    private val local: GithubDao = DatabaseProvider.githubDao(context),
) {

    suspend fun getOrganizationRepositories(organization: String): List<GithubRepoEntity> {
        return withContext(Dispatchers.IO) {
            try {
                local.getAll().ifEmpty {
                    // remote
                    val remoteData = remote.organization(organization)
                    // save entity
                    local.upsert(*remoteData.toTypedArray())
                    remoteData
                }.map {
                    GithubRepoEntityMapper.toEntity(it)
                }
            } catch (e: Exception) {
                Timber.w(e)
                emptyList()
            }
        }
    }
}

/**
 * remote
 */
// api
interface GithubService {
    @GET("/orgs/{org}/repos")
    suspend fun organization(@Path("org") org: String): List<GithubRepoResponse>
}

// client (retrofit, okhttp)
object Service {
    private val client: OkHttpClient = createOkhttpClient()
    val githubService: GithubService = createGithubService()

    private fun createOkhttpClient(): OkHttpClient {
        // create http client
        val httpClient = OkHttpClient.Builder()
            .addInterceptor(Interceptor { chain ->
                val original = chain.request()

                //header
                // ref : https://docs.github.com/ja/free-pro-team@latest/rest/repos/repos?apiVersion=2022-11-28#list-organization-repositories
                val request = original.newBuilder()
                    .header("Accept", "application/vnd.github+json")
                    .header("X-GitHub-Api-Version", "2022-11-28")
                    .method(original.method, original.body)
                    .build()

                return@Interceptor chain.proceed(request)
            })
            .readTimeout(30, TimeUnit.SECONDS)

        // log interceptor
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
        httpClient.addInterceptor(loggingInterceptor)

        return httpClient.build()
    }

    private fun createGithubService(useGson: Boolean = false): GithubService {
        val converter = if (useGson) {
            val gson = GsonBuilder().serializeNulls().create()
            GsonConverterFactory.create(gson)
        } else {
            val formatter = Json { ignoreUnknownKeys = true }
            formatter.asConverterFactory("application/json".toMediaType())
        }

        // create retrofit
        val builder = Retrofit.Builder()
            .baseUrl("https://api.github.com")
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(converter)
            .client(client)

        return builder.build().create(GithubService::class.java)
    }
}

/**
 * local
 */
@Dao
interface GithubDao {
    @Query("SELECT * FROM GithubRepoResponse")
    suspend fun getAll(): List<GithubRepoResponse>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(vararg repository: GithubRepoResponse)

    @Delete
    suspend fun delete(vararg repository: GithubRepoResponse)
}

@Database(
    entities = [
        GithubRepoResponse::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun githubDao(): GithubDao
}

object DatabaseProvider {
    private var db: AppDatabase? = null

    private fun db(context: Context): AppDatabase {
        if (db != null) return db!!
        db = Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "app-database"
        ).build()
        return db!!
    }

    fun githubDao(context: Context) = db(context).githubDao()
}
