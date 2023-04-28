package us.mitene.practicalexam.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import us.mitene.practicalexam.data.GithubRepoEntity
import us.mitene.practicalexam.data.GithubRepository
import us.mitene.practicalexam.ui.theme.PracticalexamTheme

@Composable
fun GithubRepoListScreen(
    modifier: Modifier = Modifier,
    repository: GithubRepository,
    viewModel: ComposeViewModel = viewModel(factory = ComposeViewModelFactory(repository))
) {
    val uiState = viewModel.uiState

    GithubRepoListScreen(modifier, uiState.repos)
    LaunchedEffect(Unit) {
        viewModel.fetchRepos()
    }
}

@Composable
fun GithubRepoListScreen(
    modifier: Modifier = Modifier,
    repos: List<GithubRepoEntity>
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.padding(vertical = 4.dp)
    ) {
        items(repos) { item ->
            GithubRepoItem(item.name, item.url)
        }
    }
}

@Composable
fun GithubRepoItem(
    text: String,
    url: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        Text(text = text, Modifier.padding(4.dp))
        Text(text = url, Modifier.padding(4.dp))
    }
}

@Preview
@Composable
fun GithubRepoListScreenPreview() {
    val repos = listOf(
        GithubRepoEntity("name", "url"),
        GithubRepoEntity("name", "url"),
        GithubRepoEntity("name", "url"),
        GithubRepoEntity("name", "url"),
    )
    PracticalexamTheme {
        GithubRepoListScreen(repos = repos)
    }
}

@Preview
@Composable
fun RepositoryItemPreview() {
    PracticalexamTheme {
        GithubRepoItem("repo", "url")
    }
}
