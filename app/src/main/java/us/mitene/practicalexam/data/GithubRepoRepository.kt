package us.mitene.practicalexam.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import us.mitene.practicalexam.datastore.GithubRepoLocalDataSource
import us.mitene.practicalexam.network.GithubRepoRemoteDataSource
import java.time.ZonedDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GithubRepoRepository @Inject constructor(
    private val githubRepoLocalDataSource: GithubRepoLocalDataSource,
    private val githubRepoRemoteDataSource: GithubRepoRemoteDataSource,
) {
    private val reposCache = githubRepoLocalDataSource.repos

    suspend fun getNames(): Flow<List<String>> {
        val cache = reposCache.first()
        val now = ZonedDateTime.now().toEpochSecond() - cache.fetchedAt

        if (now > CACHE_DURATION) {
            val repos = githubRepoRemoteDataSource.getRepos()
            githubRepoLocalDataSource.storeRepos(repos)

            return reposCache.map { it.namesList }
        }

        return flowOf(cache.namesList)
    }

    companion object {
        private const val CACHE_DURATION = 30 // 秒で指定
    }
}