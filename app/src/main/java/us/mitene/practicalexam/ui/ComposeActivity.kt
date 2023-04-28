package us.mitene.practicalexam.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import us.mitene.practicalexam.data.GithubRepoEntity
import us.mitene.practicalexam.data.GithubRepository
import us.mitene.practicalexam.ui.theme.PracticalexamTheme

class ComposeActivity : ComponentActivity() {
    private val repository = GithubRepository(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PracticalexamTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    GithubRepoListScreen(repository = repository)
                }
            }
        }
    }
}

@Suppress("UNCHECKED_CAST")
class ComposeViewModelFactory(
    private val repository: GithubRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ComposeViewModel(repository) as T
    }
}

class ComposeViewModel(
    private val repository: GithubRepository
) : ViewModel() {
    var uiState by mutableStateOf(ComposeUiState())

    fun fetchRepos(organization: String = "mixi-inc") {
        viewModelScope.launch {
            uiState = uiState.copy(repos = repository.getOrganizationRepositories(organization))
        }
    }
}

data class ComposeUiState(
    val repos: List<GithubRepoEntity> = emptyList()
)