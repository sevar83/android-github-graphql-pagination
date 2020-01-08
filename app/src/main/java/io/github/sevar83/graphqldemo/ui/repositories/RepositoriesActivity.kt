package io.github.sevar83.graphqldemo.ui.repositories

import android.os.Bundle
import android.widget.LinearLayout
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.github.sevar83.graphqldemo.BuildConfig
import io.github.sevar83.graphqldemo.R
import io.github.sevar83.graphqldemo.ui.repositoryDetail.RepositoryDetailActivity
import kotlinx.android.synthetic.main.activity_repositories.*

class RepositoriesActivity : AppCompatActivity() {

    private val viewModel by viewModels<RepositoriesViewModel>()

    private lateinit var repositoriesAdapter: RepositoriesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_repositories)

        if (BuildConfig.GITHUB_OAUTH_TOKEN == null) {
            tvError.isVisible = true
            tvError.text =
                """Please paste your GitHub OAuth token (generate one in GitHub settings if you don't have one) in apollo-kotlin-samples/github_token.

                   https://help.github.com/articles/creating-a-personal-access-token-for-the-command-line/""".trimIndent()
            rvRepositories.isVisible = false
            progressBar.isVisible = false
            return
        }

        tvError.isVisible = false
        progressBar.isVisible = false
        rvRepositories.isVisible = true

        rvRepositories.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        rvRepositories.addItemDecoration(DividerItemDecoration(this, LinearLayout.VERTICAL))
        repositoriesAdapter =
            RepositoriesAdapter { repositoryFragment ->
                RepositoryDetailActivity.start(this@RepositoriesActivity, repositoryFragment.name)
            }
        rvRepositories.adapter = repositoriesAdapter

        viewModel.repositories.observe(this, Observer { pagedList -> repositoriesAdapter.submitList(pagedList) })
    }
}