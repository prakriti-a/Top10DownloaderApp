package com.prakriti.top10downloader

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.util.*

private const val TAG = "FeedViewModel"

val EMPTY_FEED_LIST: List<FeedEntry> = Collections.emptyList() // type safe, can be used by other classes too

class FeedViewModel: ViewModel(), DownloadData.DownloaderCallback {


    // create an instance for async task
    private var downloadData : DownloadData? = null // by lazy { DownloadData(this, listView) }
    // can't pass listview directly as its not created before onCreate(), so use by lazy{} -> for access after onCreate()
    // issue - cant use same Async task instance twice, so declare as nullable and init later as new object when req, change to var

    private var feedCachedUrl = "INVALIDATED" // to check if the currently displaying url is being refreshed, avoid redundant download

    private val feed = MutableLiveData<List<FeedEntry>>()
    val feedEntries: LiveData<List<FeedEntry>> get() = feed

    init {
        //returns null until feed is initialised, so init it here to avoid errors
        feed.postValue(EMPTY_FEED_LIST) // -> get immutable empty list provided by Collections class
    }


    fun downloadUrl(feedUrl: String) {
        Log.i(TAG, "downloadUrl called with $feedUrl")
        if(feedUrl != feedCachedUrl) {
            downloadData = DownloadData(this) //, listView)
            downloadData?.execute(feedUrl) // use safe call as its declared nullable type
            feedCachedUrl = feedUrl
            Log.i(TAG, "downloadUrl: async task called")
        } else { // stored - so dont download again
            Log.d(TAG, "downloadUrl: URL not changed")
        }

    }

    fun invalidateUrl() { // for refresh button
        feedCachedUrl = "INVALIDATED"
    }

    override fun onDataAvailable(data: List<FeedEntry>) {
        Log.i(TAG, "onDataAvailable called")
        feed.value = data
    }

    override fun onCleared() { // called when VM is not used
        // cancel downloads here
        Log.d(TAG, "onCleared: cancelling pending donwloads")
        downloadData?.cancel(true)
    }



}