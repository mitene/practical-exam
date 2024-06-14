package us.mitene.practicalexam.ui

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class GithubReposViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(GithubReposUiState(emptyList()))
    val uiState = _uiState.asStateFlow()

    init {
        fetch()
    }

    fun fetch() {
        _uiState.value = GithubReposUiState((1..200).map { it.toString() }.toList())
    }
}

data class GithubReposUiState(
    val repoNames: List<String>
)