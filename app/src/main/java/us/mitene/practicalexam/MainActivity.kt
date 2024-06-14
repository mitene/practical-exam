package us.mitene.practicalexam

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.compose.setContent
import dagger.hilt.android.AndroidEntryPoint
import us.mitene.practicalexam.ui.GithubReposScreen
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
//        setContentView(R.layout.activity_main)
    }
}