package io.github.sevar83.graphqldemo.data

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.rx2.rxQuery
import io.github.sevar83.graphqldemo.GithubRepositoryDetailQuery
import io.github.sevar83.graphqldemo.type.PullRequestState
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject

/**
 * Created by Svetlozar Kostadinov on 05.01.20.
 */
class RepositoryDetailDataSource(
    private val apolloClient: ApolloClient,
    private val compositeDisposable: CompositeDisposable = CompositeDisposable(),
    private val processScheduler: Scheduler = Schedulers.io(),
    private val resultScheduler: Scheduler = AndroidSchedulers.mainThread()
) {

    protected val repositoryDetailSubject: PublishSubject<Response<GithubRepositoryDetailQuery.Data>> = PublishSubject.create()
    protected val exceptionSubject: PublishSubject<Throwable> = PublishSubject.create()

    val repositoryDetail: Observable<Response<GithubRepositoryDetailQuery.Data>> = repositoryDetailSubject.hide()
    val error: Observable<Throwable> = exceptionSubject.hide()

    fun fetchRepositoryDetail(repositoryName: String) {
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
}