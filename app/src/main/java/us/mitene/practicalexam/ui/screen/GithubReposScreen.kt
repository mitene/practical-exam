package us.mitene.practicalexam.ui.screen

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun GithubReposScreen(
    githubReposViewModel: GithubReposViewModel = hiltViewModel()
) {
    val githubReposUiState by githubReposViewModel.uiState.collectAsState()

    Scaffold {
        GithubReposLayout(
            repoNames = githubReposUiState.repoNames,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(it)
        )
    }
}

@Composable
fun GithubReposLayout(
    repoNames: List<String>,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier = modifier) {
        items(repoNames) { name ->
            Text(text = name)
        }
    }
}

@Preview
@Composable
private fun Preview() {
    Scaffold { paddingValues ->
        GithubReposLayout(
            repoNames = (1..200).map { it.toString() }.toList(),
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight() // TODO: どんな効果があるか調べる
                .padding(paddingValues)
        )
    }
}