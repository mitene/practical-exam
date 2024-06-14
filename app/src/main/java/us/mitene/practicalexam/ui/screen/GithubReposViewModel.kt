package us.mitene.practicalexam.ui.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import us.mitene.practicalexam.network.GithubApi
import javax.inject.Inject

@HiltViewModel
class GithubReposViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(GithubReposUiState(emptyList()))
    val uiState = _uiState.asStateFlow()

    init {
        fetch()
    }

    fun fetch() {
        viewModelScope.launch {
            val result = GithubApi.retrofitService.getRepos()
            _uiState.value = GithubReposUiState(
                result.map { it.name }
            )
        }
    }
}

data class GithubReposUiState(
    val repoNames: List<String>
)