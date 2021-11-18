package com.example.rxjava

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract.CommonDataKinds.Event
import android.util.Log
import androidx.lifecycle.lifecycleScope
import io.reactivex.BackpressureStrategy.BUFFER
import io.reactivex.BackpressureStrategy.DROP
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.ObservableOnSubscribe
import io.reactivex.Observer
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.*
import kotlinx.coroutines.rx2.await
import java.util.concurrent.TimeUnit.SECONDS
import javax.security.auth.Subject

const val TAG = "ReactiveDemo"

class MainActivity : AppCompatActivity() {

    private val disposables = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        justExample()
//        createExample()
//        intervalTest()
//        backpressureExample()
    }

    private fun justExample() {

        printToLog("From just:")
        Observable.just("One", "Two", "Three")
            .subscribe(
                { onNext -> printToLog(onNext) },
                { onError -> onError.message?.let { printToLog(it) } },
                { printToLog("onComplete") }
            )
            .dispose()
    }

    private fun createExample() {
        printEndLine()
        printToLog("From create with correct list:")
        val correctList = listOf("One", "Two", "Three")
        getObservableFromList(correctList).subscribe {
            printToLog(it)
        }.dispose()

        printEndLine()
        printToLog("From create with incorrect list:")
        val incorrectList = listOf("One", "", "Three")
        getObservableFromList(incorrectList).subscribe(
            {
                printToLog(it)
            },
            {
                it.message?.let { printToLog(it) }
            }).dispose()
    }

    private fun intervalTest() {
        printEndLine()
        printToLog("Intervals test:")
        disposables.add(Observable.intervalRange(
            10L,
            5L,
            0L,
            1L,
            SECONDS
        ).subscribe {
            printToLog(it.toString())
        })
        //.dispose() остановит работу эмиттера до получения значений. Используем CompositeDisposable.
    }

    override fun onDestroy() {
        super.onDestroy()
        disposables.clear()
    }

    private fun backpressureExample() {
        val observable = Observable.create<Int> { emitter ->
            repeat(1000000000) {
                emitter.onNext(it)
            }
            emitter.onComplete()
        }
        observable.subscribeOn(Schedulers.computation())
            .subscribe({
//                printToLog(it.toString())
            }, {
                it.message?.let { printToLog(it) }
            }, {
                printToLog("completed")
            })

        /**
         * Почему-то оно не ломается, хотя должно выкидывать OutOfMemory. В любом случае решение ниже
         */
        observable.subscribeOn(Schedulers.computation())
            .toFlowable(DROP)
            .subscribe({
//                printToLog(it.toString())
            }, {
                it.message?.let { printToLog(it) }
            }, {
                printToLog("completed")
            })
    }

    private fun getObservableFromList(list: List<String>) =
        Observable.create<String> { emitter ->
            list.forEach { item ->
                if (item.isEmpty()) {
                    emitter.onError(Exception("Empty string!"))
                }
                emitter.onNext(item)
            }
            emitter.onComplete()
        }

    private fun printToLog(text: String) {
        Log.i(TAG, text)
    }

    private fun printEndLine() {
        Log.i(TAG, " ")
    }
}