package io.github.sevar83.graphqldemo.data

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Response
import io.github.sevar83.graphqldemo.GithubRepositoriesQuery
import io.github.sevar83.graphqldemo.GithubRepositoryCommitsQuery
import io.github.sevar83.graphqldemo.GithubRepositoryDetailQuery
import io.github.sevar83.graphqldemo.fragment.RepositoryFragment
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

/**
 * This is a base class defining the required behavior for a data source of GitHub information. We don't care if that data
 * is fetched via RxJava, coroutines, etc. Any implementations of this can fetch data however they want, and post that result
 * to the public Observables that activities can subscribe to for information.
 */
abstract class GitHubDataSource(protected val apolloClient: ApolloClient) {
    protected val repositoriesSubject: PublishSubject<List<RepositoryFragment>> = PublishSubject.create()
    protected val repositoryDetailSubject: PublishSubject<Response<GithubRepositoryDetailQuery.Data>> = PublishSubject.create()
    protected val commitsSubject: PublishSubject<List<GithubRepositoryCommitsQuery.Edge>> = PublishSubject.create()
    protected val exceptionSubject: PublishSubject<Throwable> = PublishSubject.create()

    val repositories: Observable<List<RepositoryFragment>> = repositoriesSubject.hide()
    val repositoryDetail: Observable<Response<GithubRepositoryDetailQuery.Data>> = repositoryDetailSubject.hide()
    val commits: Observable<List<GithubRepositoryCommitsQuery.Edge>> = commitsSubject.hide()
    val error: Observable<Throwable> = exceptionSubject.hide()

    abstract fun fetchRepositories()
    abstract fun fetchRepositoryDetail(repositoryName: String)
    abstract fun fetchCommits(repositoryName: String)
    abstract fun cancelFetching()

    protected fun mapRepositoriesResponseToRepositories(response: Response<GithubRepositoriesQuery.Data>): List<RepositoryFragment> {
        return response.data()?.viewer?.repositories?.nodes
            ?.mapNotNull { node -> node?.fragments?.repositoryFragment }
            ?: emptyList()
    }

    companion object {
        const val DEFAULT_PAGE_SIZE = 10
    }
}