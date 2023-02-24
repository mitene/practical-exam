package us.mitene.practicalexam

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.*
import androidx.room.Database
import com.google.gson.GsonBuilder
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
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
import us.mitene.practicalexam.databinding.ActivityMainBinding
import us.mitene.practicalexam.databinding.ListItemRepositoryBinding
import java.lang.Exception
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // setup timber
        Timber.plant(Timber.DebugTree())

        val adapter = RepositoryAdapter()

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        // fetch
        lifecycleScope.launchWhenCreated {
            val entities = MainRepository(this@MainActivity).getOrganizationRepositories()
            adapter.update(entities)
        }
    }
}

class MainRepository(context: Context) {
    private val remote = Service.githubService
    private val local = DatabaseProvider.githubDao(context)

    suspend fun getOrganizationRepositories(): List<RepositoryEntity> {
        return withContext(Dispatchers.IO) {
            try {
                local.getAll().ifEmpty {
                    // remote
                    val remoteData = remote.organization("mixi-inc")
                    // save entity
                    local.upsert(*remoteData.toTypedArray())
                    remoteData
                }.map {
                    RepositoryMapper.toEntity(it)
                }
            } catch (e: Exception) {
                Timber.w(e)
                emptyList()
            }
        }
    }
}

// api client (retrofit, okhttp)
object Service {
    private val client: OkHttpClient = createOkhttpClient()
    val githubService: GithubService = createGithubService()

    private fun createOkhttpClient(): OkHttpClient {
        // create http client
        val httpClient = OkHttpClient.Builder()
            .addInterceptor(Interceptor { chain ->
                val original = chain.request()

                //header
                val request = original.newBuilder()
                    .header("Accept", "vnd.github.v3+json")
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

    @OptIn(ExperimentalSerializationApi::class)
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

// entity
@Serializable
@Entity
data class RepositoryResponse(
    @PrimaryKey
    val id: Int,
    val name: String,
    val url: String,
)

data class RepositoryEntity(
    val name: String,
    val url: String,
)

object RepositoryMapper {
    fun toEntity(response: RepositoryResponse) = RepositoryEntity(
        name = response.name,
        url = response.url,
    )
}

// room
@Dao
interface GithubDao {
    @Query("SELECT * FROM RepositoryResponse")
    suspend fun getAll(): List<RepositoryResponse>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(vararg repository: RepositoryResponse)

    @Delete
    suspend fun delete(vararg repository: RepositoryResponse)
}

@Database(
    entities = [
        RepositoryResponse::class
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

// adapter
class RepositoryAdapter : RecyclerView.Adapter<RepositoryAdapter.ViewHolder>() {
    private var repositories = emptyList<RepositoryEntity>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ListItemRepositoryBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(repositories[position])
    }

    override fun getItemCount(): Int = repositories.size

    fun update(data: List<RepositoryEntity>) {
        repositories = data
        notifyItemRangeChanged(0, repositories.size)
    }

    class ViewHolder(
        private val binding: ListItemRepositoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: RepositoryEntity) {
            binding.name.text = item.name
            binding.url.text = item.url
            binding.root.setOnClickListener {
                Timber.d("TESTTEST  ${item.name}")
            }
        }
    }
}

interface GithubService {
    @GET("/orgs/{org}/repos")
    suspend fun organization(@Path("org") org: String): List<RepositoryResponse>
}