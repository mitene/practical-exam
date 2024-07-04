package us.mitene.practicalexam.ui.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import us.mitene.practicalexam.data.GithubRepoRepository
import javax.inject.Inject

@HiltViewModel
class GithubReposViewModel @Inject constructor(
    private val githubRepoRepository: GithubRepoRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(GithubReposUiState(emptyList()))
    val uiState = _uiState.asStateFlow()

    init {
        fetch()
    }

    private fun fetch() {
        viewModelScope.launch {
            githubRepoRepository.getNames().collect {
                _uiState.value = GithubReposUiState(it)
            }
        }
    }
}

data class GithubReposUiState(
    val repoNames: List<String>
)