package io.github.sevar83.graphqldemo.ui.repositories

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import io.github.sevar83.graphqldemo.KotlinSampleApp
import io.github.sevar83.graphqldemo.data.GitHubConstants
import io.github.sevar83.graphqldemo.fragment.RepositoryFragment


/**
 * Created by Svetlozar Kostadinov on 05.01.20.
 */
class RepositoriesViewModel(application: Application) : AndroidViewModel(application) {

    private val dataSourceFactory: DataSource.Factory<String, RepositoryFragment> by lazy {
        getApplication<KotlinSampleApp>().getRepositoriesDataSourceFactory()
    }

    val repositories: LiveData<PagedList<RepositoryFragment>>
            = LivePagedListBuilder(dataSourceFactory, GitHubConstants.DEFAULT_PAGE_SIZE).build()
}