package us.mitene.practicalexam.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import timber.log.Timber
import us.mitene.practicalexam.R
import us.mitene.practicalexam.data.GithubRepoEntity
import us.mitene.practicalexam.data.GithubRepository
import us.mitene.practicalexam.databinding.ActivityNormalBinding
import us.mitene.practicalexam.databinding.ListItemRepositoryBinding

class NormalActivity : AppCompatActivity() {
    lateinit var binding: ActivityNormalBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val adapter = GithubRepoAdapter()

        binding = DataBindingUtil.setContentView(this, R.layout.activity_normal)
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                // fetch data
                val context = this@NormalActivity
                val entities = GithubRepository(context).getOrganizationRepositories("mixi-inc")
                adapter.update(entities)
            }
        }
    }
}

// adapter
class GithubRepoAdapter : RecyclerView.Adapter<GithubRepoAdapter.ViewHolder>() {
    private var repositories = emptyList<GithubRepoEntity>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ListItemRepositoryBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(repositories[position])
    }

    override fun getItemCount(): Int = repositories.size

    fun update(data: List<GithubRepoEntity>) {
        repositories = data
        notifyItemRangeChanged(0, repositories.size)
    }

    class ViewHolder(
        private val binding: ListItemRepositoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: GithubRepoEntity) {
            binding.name.text = item.name
            binding.url.text = item.url
            binding.root.setOnClickListener {
                Timber.d("TESTTEST  ${item.name}")
            }
        }
    }
}
