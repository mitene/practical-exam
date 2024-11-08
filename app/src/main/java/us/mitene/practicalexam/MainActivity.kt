package us.mitene.practicalexam

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import us.mitene.practicalexam.databinding.ActivityMainBinding
import us.mitene.practicalexam.ui.ComposeActivity
import us.mitene.practicalexam.ui.NormalActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.normal.setOnClickListener {
            startActivity(Intent(this, NormalActivity::class.java))
        }
        binding.compose.setOnClickListener {
            startActivity(Intent(this, ComposeActivity::class.java))
        }
    }
}
