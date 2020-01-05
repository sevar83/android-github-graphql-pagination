package io.github.sevar83.graphqldemo.ui.repositories

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.github.sevar83.graphqldemo.BuildConfig
import io.github.sevar83.graphqldemo.KotlinSampleApp
import io.github.sevar83.graphqldemo.R
import io.github.sevar83.graphqldemo.data.GitHubDataSource
import io.github.sevar83.graphqldemo.fragment.RepositoryFragment
import io.github.sevar83.graphqldemo.ui.repositoryDetail.RepositoryDetailActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*

class RepositoriesActivity : AppCompatActivity() {

    private lateinit var repositoriesAdapter: RepositoriesAdapter
    private val compositeDisposable = CompositeDisposable()
    private val dataSource: GitHubDataSource by lazy {
        (application as KotlinSampleApp).getDataSource()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (BuildConfig.GITHUB_OAUTH_TOKEN == null) {
            tvError.visibility = View.VISIBLE
            tvError.text =
                "Please paste your GitHub OAuth token (generate one in GitHub settings if you don't have one) in apollo-kotlin-samples/github_token.\n\nhttps://help.github.com/articles/creating-a-personal-access-token-for-the-command-line/"
            rvRepositories.visibility = View.GONE
            progressBar.visibility = View.GONE
            return
        }

        tvError.visibility = View.GONE

        rvRepositories.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        rvRepositories.addItemDecoration(DividerItemDecoration(this, LinearLayout.VERTICAL))
        repositoriesAdapter =
            RepositoriesAdapter { repositoryFragment ->
                RepositoryDetailActivity.start(this@RepositoriesActivity, repositoryFragment.name)
            }
        rvRepositories.adapter = repositoriesAdapter

        setupDataSource()
        fetchRepositories()
    }

    private fun setupDataSource() {
        val successDisposable = dataSource.repositories
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(this::handleRepositories)

        val errorDisposable = dataSource.error
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(this::handleError)

        compositeDisposable.add(successDisposable)
        compositeDisposable.add(errorDisposable)
    }

    private fun handleRepositories(repos: List<RepositoryFragment>) {
        progressBar.visibility = View.GONE
        rvRepositories.visibility = View.VISIBLE
        repositoriesAdapter.setItems(repos)
    }

    private fun handleError(error: Throwable?) {
        tvError.text = error?.localizedMessage
        tvError.visibility = View.VISIBLE
        progressBar.visibility = View.GONE
        error?.printStackTrace()
    }

    private fun fetchRepositories() {
        dataSource.fetchRepositories()
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
        dataSource.cancelFetching()
    }
}