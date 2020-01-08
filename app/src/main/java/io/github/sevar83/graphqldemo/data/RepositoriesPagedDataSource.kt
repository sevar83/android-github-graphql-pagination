package io.github.sevar83.graphqldemo.data

import androidx.paging.DataSource
import androidx.paging.PageKeyedDataSource
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Input
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.rx2.rxQuery
import io.github.sevar83.graphqldemo.GithubRepositoriesAfterQuery
import io.github.sevar83.graphqldemo.GithubRepositoriesBeforeQuery
import io.github.sevar83.graphqldemo.fragment.RepositoryFragment
import io.github.sevar83.graphqldemo.type.OrderDirection
import io.github.sevar83.graphqldemo.type.RepositoryOrderField
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Cancellable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject

/**
 * Created by Svetlozar Kostadinov on 05.01.20.
 */
class RepositoriesPagedDataSource(
    private val apolloClient: ApolloClient,
    private val compositeDisposable: CompositeDisposable = CompositeDisposable(),
    private val processScheduler: Scheduler = Schedulers.io(),
    private val resultScheduler: Scheduler = AndroidSchedulers.mainThread()
) : PageKeyedDataSource<String, RepositoryFragment>(), Cancellable {

    private val repositoriesSubject: PublishSubject<List<RepositoryFragment>> = PublishSubject.create()
    private val exceptionSubject: PublishSubject<Throwable> = PublishSubject.create()

    val repositories: Observable<List<RepositoryFragment>> = repositoriesSubject.hide()
    val error: Observable<Throwable> = exceptionSubject.hide()

    private val orderBy get() = RepositoryOrderField.STARGAZERS
    private val orderDirection = OrderDirection.DESC

    override fun cancel() {
        compositeDisposable.dispose()
    }

    // PageKeyedDataSource implementation below

    override fun loadInitial(
        params: LoadInitialParams<String>,
        callback: LoadInitialCallback<String, RepositoryFragment>
    ) {
        val disposable = Observable.just(Unit)
            .map {
                GithubRepositoriesAfterQuery(
                    repositoriesCount = GitHubConstants.DEFAULT_PAGE_SIZE,
                    orderBy = orderBy,
                    orderDirection = orderDirection,
                    after = Input.absent()
                )
            }
            .flatMap { apolloClient.rxQuery(it) }
            .subscribeOn(processScheduler)
            .observeOn(resultScheduler)
            .map(::mapNextResponseToPage)
            .subscribe(
                { page -> callback.onResult(page.data, page.prevPageKey, page.nextPageKey) },
                exceptionSubject::onNext
            )

        compositeDisposable.add(disposable)
    }

    override fun loadAfter(
        params: LoadParams<String>,
        callback: LoadCallback<String, RepositoryFragment>
    ) {
        val disposable = Observable.just(Unit)
            .map {
                GithubRepositoriesAfterQuery(
                    repositoriesCount = GitHubConstants.DEFAULT_PAGE_SIZE,
                    orderBy = orderBy,
                    orderDirection = orderDirection,
                    after = Input.fromNullable(params.key)
                )
            }
            .flatMap { apolloClient.rxQuery(it) }
            .subscribeOn(processScheduler)
            .observeOn(resultScheduler)
            .map(::mapNextResponseToPage)
            .subscribe(
                { page -> callback.onResult(page.data, page.nextPageKey) },
                exceptionSubject::onNext
            )

        compositeDisposable.add(disposable)
    }

    override fun loadBefore(
        params: LoadParams<String>,
        callback: LoadCallback<String, RepositoryFragment>
    ) {
        val disposable = Observable.just(Unit)
            .map {
                GithubRepositoriesBeforeQuery(
                    repositoriesCount = GitHubConstants.DEFAULT_PAGE_SIZE,
                    orderBy = orderBy,
                    orderDirection = orderDirection,
                    before = Input.fromNullable(params.key)
                )
            }
            .flatMap { apolloClient.rxQuery(it) }
            .subscribeOn(processScheduler)
            .observeOn(resultScheduler)
            .map(::mapPreviousResponseToPage)
            .subscribe(
                { page -> callback.onResult(page.data, page.prevPageKey) },
                exceptionSubject::onNext
            )

        compositeDisposable.add(disposable)
    }

    private fun mapNextResponseToPage(
        response: Response<GithubRepositoriesAfterQuery.Data>
    ): Page<String, RepositoryFragment> {

        val hasNextPage = response.data()?.viewer?.repositories?.pageInfo?.hasNextPage ?: false
        val endCursor: String? = response.data()?.viewer?.repositories?.pageInfo?.endCursor

        return Page(
            data = response.data()?.viewer?.repositories?.nodes
                ?.mapNotNull { node -> node?.fragments?.repositoryFragment }
                ?: emptyList(),
            nextPageKey = endCursor?.takeIf { hasNextPage }
        )
    }

    private fun mapPreviousResponseToPage(
        response: Response<GithubRepositoriesBeforeQuery.Data>
    ): Page<String, RepositoryFragment> {

        val hasPreviousPage = response.data()?.viewer?.repositories?.pageInfo?.hasPreviousPage ?: false
        val startCursor: String? = response.data()?.viewer?.repositories?.pageInfo?.startCursor

        return Page(
            data = response.data()?.viewer?.repositories?.nodes
                ?.mapNotNull { node -> node?.fragments?.repositoryFragment }
                ?: emptyList(),
            nextPageKey = startCursor?.takeIf { hasPreviousPage }
        )
    }

    class Factory(
        private val apolloClient: ApolloClient,
        private val compositeDisposable: CompositeDisposable = CompositeDisposable(),
        private val processScheduler: Scheduler = Schedulers.io(),
        private val resultScheduler: Scheduler = AndroidSchedulers.mainThread()
    ) : DataSource.Factory<String, RepositoryFragment>() {

        override fun create(): DataSource<String, RepositoryFragment> {
            return RepositoriesPagedDataSource(
                apolloClient,
                compositeDisposable,
                processScheduler,
                resultScheduler
            )
        }
    }

    private data class Page<Key, Value>(
        val data: List<Value>,
        val nextPageKey: Key? = null,
        val prevPageKey: Key? = null
    )
}