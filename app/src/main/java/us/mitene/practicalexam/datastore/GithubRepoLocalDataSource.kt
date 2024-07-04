package us.mitene.practicalexam.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.withContext
import timber.log.Timber
import us.mitene.practicalexam.datastore.proto.GithubRepoCache
import us.mitene.practicalexam.di.IoDispatcher
import us.mitene.practicalexam.network.GithubRepo
import java.io.IOException
import java.time.ZonedDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GithubRepoLocalDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
    @IoDispatcher private val dispatcher: CoroutineDispatcher,
) {
    private val Context.githubRepoStore: DataStore<GithubRepoCache> by dataStore(
        fileName = DATA_STORE_FILE_NAME,
        serializer = GithubRepoCacheSerializer
    )

    val repos = context.githubRepoStore.data
        .catch { exception ->
            // dataStore.data throws an IOException when an error is encountered when reading data
            if (exception is IOException) {
                Timber.tag(TAG).e(exception, "Error reading sort order preferences.")
                emit(GithubRepoCache.getDefaultInstance())
            } else {
                throw exception
            }
        }

    suspend fun storeRepos(repos: List<GithubRepo>) = withContext(dispatcher) {
        context.githubRepoStore.updateData { cache ->
            val names = repos.map { it.name }
            val fetchedAt = ZonedDateTime.now().toEpochSecond()

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
    }
}