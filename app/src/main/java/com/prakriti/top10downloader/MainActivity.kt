package com.prakriti.top10downloader

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.activity_main.*

// edited to use ViewModel

private const val TAG = "MainActivity"

// state changes
private const val FEED_URL_KEY = "FeedUrl"
private const val FEED_LIMIT_KEY = "FeedLimit"

class MainActivity : AppCompatActivity() {
// kotlin vars are public by default
    // integrate a progress bar before each feed loads
    // find a way to avoid download on rotation

    // building url is job of MainActivity as it happens from UI interaction
    // so dont move these to VM
    private var feedUrl: String = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topfreeapplications/limit=%d/xml"
        // replace int value later using formatted string
    private var feedLimit = 10

    private val feedViewModel: FeedViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) { // null if there's nothing to restore
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.i(TAG, "onCreate called")

        val feedAdapter = FeedAdapter(this, R.layout.list_item, EMPTY_FEED_LIST)
        listView.adapter = feedAdapter

        // check for bundle item, in case of orientation change
        if(savedInstanceState != null) {
            // kotlin does smart cast to non-null bundle since we performed null check here
            feedUrl = savedInstanceState.getString(FEED_URL_KEY)!! // since getString() takes nullable String, assert not null !!
            feedLimit = savedInstanceState.getInt(FEED_LIMIT_KEY)
        }

        // update UI
        feedViewModel.feedEntries.observe(this, Observer { feedEntries ->
            feedAdapter.setFeedList(feedEntries) // here the list is not null, initialised to empty list
            // if error^, pass feedEntries ?: EMPTY_FEED_LIST (elvis op)
        })

//        val initUrl = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topfreeapplications/limit=10/xml"
        feedViewModel.downloadUrl(feedUrl.format(feedLimit))
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean { // inflate menu
        menuInflater.inflate(R.menu.feeds_menu, menu) // no getter or setter like java, directly call property
        if(feedLimit == 10) {
            menu.findItem(R.id.menu10)?.isChecked = true // menu is nullable type
        } else {
            menu.findItem(R.id.menu25)?.isChecked = true
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean { // called when menu item is selected
        when(item.itemId) {
            /*
            CHALLENGE: data is downloaded even when same menu option is clicked again -> redundant download
            also, re-downloads data on orientation change, and resets limit to 10
             */
            R.id.menuRefresh -> feedViewModel.invalidateUrl()
                // feedCachedUrl = "INVALIDATED" // so download is performed again explicitly
            R.id.menuFree -> feedUrl = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topfreeapplications/limit=%d/xml"
            R.id.menuPaid -> feedUrl = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/toppaidapplications/limit=%d/xml"
            R.id.menuSongs -> feedUrl = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topsongs/limit=%d/xml"
            R.id.menu10, R.id.menu25 -> {
                // items need to be checked/unchecked in code, not when user clicks them
                if(item.isChecked) {
                    Log.d(TAG, "onOptionsItemSelected: no change, limit is: $feedLimit")
                } else { // set checked now
                    item.isChecked = true
                    feedLimit = 35 - feedLimit
                    Log.d(TAG, "onOptionsItemSelected: item checked, limit changed $feedLimit")
                }
            }
            else -> return super.onOptionsItemSelected(item)
            // always write else clause
        }
        feedViewModel.downloadUrl(feedUrl.format(feedLimit))
        return true
    }

    override fun onSaveInstanceState(outState: Bundle) { // retrieve in onCreate, as this is for saving state in orientation change
        super.onSaveInstanceState(outState)
        outState.putString(FEED_URL_KEY, feedUrl)
        outState.putInt(FEED_LIMIT_KEY, feedLimit)
    }






/*  override fun onDestroy() { // in case activity is destroyed while async task is running
        super.onDestroy()
        Log.d(TAG, "onDestroy called")
        downloadData?.cancel(true)
    }
    // removed as MainActivity no longer handles downloading
 */

    //companion object { // making class static == move to companion object, to avoid memory leaks
    //}

}

/*
Inner class -> holds reference to an activity
Static nested class -> exists independently
 */