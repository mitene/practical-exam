package us.mitene.practicalexam.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import timber.log.Timber
import us.mitene.practicalexam.datastore.proto.GithubRepoCache
import us.mitene.practicalexam.network.GithubApiDataSource
import java.io.IOException
import java.time.ZonedDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GithubRepoCacheRepository @Inject constructor(
    private val github: GithubApiDataSource,
    @ApplicationContext private val context: Context,
) {
    private val Context.githubRepoCacheStore: DataStore<GithubRepoCache> by dataStore(
        fileName = DATA_STORE_FILE_NAME,
        serializer = GithubRepoCacheSerializer
    )
    private val repoCacheFlow = context.githubRepoCacheStore.data
        .catch { exception ->
            // dataStore.data throws an IOException when an error is encountered when reading data
            if (exception is IOException) {
                Timber.tag(TAG).e(exception, "Error reading sort order preferences.")
                emit(GithubRepoCache.getDefaultInstance())
            } else {
                throw exception
            }
        }

    suspend fun getNames(): Flow<List<String>> {
        val cache = repoCacheFlow.first()
        Timber.tag(TAG).i("lastFetchedAt: ${cache.fetchedAt}")
        val now = ZonedDateTime.now().toEpochSecond() - cache.fetchedAt

        if (now > CACHE_DURATION) {
            fetch()

            return repoCacheFlow.map { it.namesList }
        }

        return flowOf(cache.namesList)
    }

    private suspend fun fetch() {
        context.githubRepoCacheStore.updateData { cache ->
            val names = github.getRepos().map { it.name }
            val fetchedAt = ZonedDateTime.now().toEpochSecond()
            Timber.tag(TAG).i("fetched cache at: $fetchedAt")

            cache.toBuilder()
                .setFetchedAt(fetchedAt)
                .clearNames()
                .addAllNames(names)
                .build()
        }
    }

    companion object {
        private const val DATA_STORE_FILE_NAME = "github_repo_cache.pb"
        private const val TAG: String = "GithubRepoCacheRepo"
        private const val CACHE_DURATION = 30 // 秒で指定
    }
}