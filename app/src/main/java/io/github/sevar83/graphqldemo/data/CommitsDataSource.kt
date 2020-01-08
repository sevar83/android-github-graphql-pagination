package io.github.sevar83.graphqldemo.data

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.cache.http.HttpCachePolicy
import com.apollographql.apollo.rx2.rxQuery
import io.github.sevar83.graphqldemo.GithubRepositoryCommitsQuery
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject

/**
 * Created by Svetlozar Kostadinov on 05.01.20.
 */
class CommitsDataSource(
    private val apolloClient: ApolloClient,
    private val compositeDisposable: CompositeDisposable = CompositeDisposable(),
    private val processScheduler: Scheduler = Schedulers.io(),
    private val resultScheduler: Scheduler = AndroidSchedulers.mainThread()
) {
    private val commitsSubject: PublishSubject<List<GithubRepositoryCommitsQuery.Edge>> = PublishSubject.create()
    private val exceptionSubject: PublishSubject<Throwable> = PublishSubject.create()

    val commits: Observable<List<GithubRepositoryCommitsQuery.Edge>> = commitsSubject.hide()
    val error: Observable<Throwable> = exceptionSubject.hide()

    fun fetchCommits(repositoryName: String) {
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

    fun cancelFetching() {
        compositeDisposable.dispose()
    }
}