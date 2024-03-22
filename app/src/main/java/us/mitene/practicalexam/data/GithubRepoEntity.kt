package us.mitene.practicalexam.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

// response
@Serializable
@Entity
data class GithubRepoResponse(
    @PrimaryKey
    val id: Int,
    val name: String,
    val url: String,
)

// entity
data class GithubRepoEntity(
    val name: String,
    val url: String,
)

// mapper
object GithubRepoEntityMapper {
    fun toEntity(response: GithubRepoResponse) = GithubRepoEntity(
        name = response.name,
        url = response.url,
    )
}

