package us.mitene.practicalexam.network

import kotlinx.serialization.Serializable

@Serializable
data class GithubRepo(
    val name: String
)