package io.github.sevar83.graphqldemo.data

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.cache.http.HttpCachePolicy
import com.apollographql.apollo.rx2.rxQuery
import io.github.sevar83.graphqldemo.GithubRepositoriesQuery
import io.github.sevar83.graphqldemo.GithubRepositoryCommitsQuery
import io.github.sevar83.graphqldemo.GithubRepositoryDetailQuery
import io.github.sevar83.graphqldemo.type.OrderDirection
import io.github.sevar83.graphqldemo.type.PullRequestState
import io.github.sevar83.graphqldemo.type.RepositoryOrderField
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

/**
 * An implementation of a [GitHubDataSource] that shows how we can use RxJava to make our apollo requests.
 */
class ApolloRxService(
    apolloClient: ApolloClient,
    private val compositeDisposable: CompositeDisposable = CompositeDisposable(),
    private val processScheduler: Scheduler = Schedulers.io(),
    private val resultScheduler: Scheduler = AndroidSchedulers.mainThread()
) : GitHubDataSource(apolloClient) {

    override fun fetchRepositories() {
        val repositoriesQuery = GithubRepositoriesQuery(
            repositoriesCount = 50,
            orderBy = RepositoryOrderField.UPDATED_AT,
            orderDirection = OrderDirection.DESC
        )

        val disposable = apolloClient.rxQuery(repositoriesQuery)
            .subscribeOn(processScheduler)
            .observeOn(resultScheduler)
            .map(this::mapRepositoriesResponseToRepositories)
            .subscribe(
                repositoriesSubject::onNext,
                exceptionSubject::onNext
            )

        compositeDisposable.add(disposable)
    }

    override fun fetchRepositoryDetail(repositoryName: String) {
        val repositoryDetailQuery = GithubRepositoryDetailQuery(
            name = repositoryName,
            pullRequestStates = listOf(PullRequestState.OPEN)
        )

        val disposable = apolloClient.rxQuery(repositoryDetailQuery)
            .subscribeOn(processScheduler)
            .observeOn(resultScheduler)
            .subscribe(
                repositoryDetailSubject::onNext,
                exceptionSubject::onNext
            )

        compositeDisposable.add(disposable)
    }

    override fun fetchCommits(repositoryName: String) {
        val commitsQuery = GithubRepositoryCommitsQuery(
            name = repositoryName
        )

        val disposable = apolloClient
            .rxQuery(commitsQuery) {
                httpCachePolicy(HttpCachePolicy.NETWORK_FIRST)
            }
            .subscribeOn(processScheduler)
            .observeOn(resultScheduler)
            .map { response ->
                val headCommit =
                    response.data()?.viewer?.repository?.ref?.target as? GithubRepositoryCommitsQuery.AsCommit
                headCommit?.history?.edges?.filterNotNull().orEmpty()
            }
            .subscribe(
                commitsSubject::onNext,
                exceptionSubject::onNext
            )

        compositeDisposable.add(disposable)
    }

    override fun cancelFetching() {
        compositeDisposable.dispose()
    }
}
