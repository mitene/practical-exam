package us.mitene.practicalexam

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import us.mitene.practicalexam.ui.screen.GithubReposScreen
import us.mitene.practicalexam.ui.theme.PracticalExamTheme

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            PracticalExamTheme {
                GithubReposScreen()
            }
        }
    }
}