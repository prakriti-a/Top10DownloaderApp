package com.prakriti.top10downloader

import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.net.URL
import kotlin.properties.Delegates

class MainActivity : AppCompatActivity() {
// kotlin vars are public by default
    // integrate a progress bar before each feed loads
    // find a way to avoid download on rotation

    private val TAG = "MainActivity"
    // create an instance for async task
    private var downloadData : DownloadData? = null // by lazy { DownloadData(this, listView) }
        // can't pass listview directly as its not created before onCreate(), so use by lazy{} -> for access after onCreate()
        // issue - cant use same Async task instance twice, so declare as nullable and init later as new object when req, change to var

    private var feedUrl: String = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topfreeapplications/limit=%d/xml"
        // replace int value later using formatted string
    private var feedLimit = 10

    private var feedCachedUrl = "INVALIDATED" // to check if the currently displaying url is being refreshed, avoid redundant download
    // state changes
    private val FEED_URL_KEY = "FeedUrl"
    private val FEED_LIMIT_KEY = "FeedLimit"


    override fun onCreate(savedInstanceState: Bundle?) { // null if there's nothing to restore
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.i(TAG, "onCreate called")

        // check for bundle item, in case of orientation change
        if(savedInstanceState != null) {
            // kotlin does smart cast to non-null bundle since we performed null check here
            feedUrl = savedInstanceState.getString(FEED_URL_KEY)!! // since getString() takes nullable String, assert not null !!
            feedLimit = savedInstanceState.getInt(FEED_LIMIT_KEY)
        }

//        val initUrl = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topfreeapplications/limit=10/xml"
        downloadUrl(feedUrl.format(feedLimit))
    }

    private fun downloadUrl(feedUrl: String) {
        if(feedUrl != feedCachedUrl) {
            downloadData = DownloadData(this, listView)
            downloadData?.execute(feedUrl) // use safe call as its declared nullable type
            feedCachedUrl = feedUrl
            Log.i(TAG, "downloadUrl: async task called")
        } else { // stored - so dont download again
            Log.d(TAG, "downloadUrl: URL not changed")
        }

    }

    // menu code
    override fun onCreateOptionsMenu(menu: Menu?): Boolean { // inflate menu
        menuInflater.inflate(R.menu.feeds_menu, menu) // no getter or setter like java, directly call property
        if(feedLimit == 10) {
            menu?.findItem(R.id.menu10)?.isChecked = true // menu is nullable type
        } else {
            menu?.findItem(R.id.menu25)?.isChecked = true
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean { // called when menu item is selected
        when(item.itemId) {
            /*
            CHALLENGE: data is downloaded even when same menu option is clicked again -> redundant download
            also, re-downloads data on orientation change, and resets limit to 10
             */
            R.id.menuRefresh -> feedCachedUrl = "INVALIDATED" // so download is performed again explicitly
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
        downloadUrl(feedUrl.format(feedLimit))
        return true
    }

    override fun onSaveInstanceState(outState: Bundle) { // retrieve in onCreate, as this is for saving state in orientation change
        super.onSaveInstanceState(outState)
        outState.putString(FEED_URL_KEY, feedUrl)
        outState.putInt(FEED_LIMIT_KEY, feedLimit)
    }


    override fun onDestroy() { // in case activity is destroyed while async task is running
        super.onDestroy()
        Log.d(TAG, "onDestroy called")
        downloadData?.cancel(true)
    }


    companion object { // making class static == move to companion object, to avoid memory leaks

        // ******* one instance of ASYNC TASK can only be executed once ********
        // inner class to access properties of this class -> not visible to any other class
        private class DownloadData(context: Context, listView: ListView) : AsyncTask<String, Void, String>() {
            // async processing
            // string url, void progressBar, string result
            private val TAG = "DownloadData"

            private var dContext: Context by Delegates.notNull() // to mitigate memory leaks
            private var dListView: ListView by Delegates.notNull() // = listView

            // init block for properties
            init {
                dContext = context
                dListView = listView
            }

            override fun doInBackground(vararg url: String?): String { // performs all these tasks in separate thread
                // vararg -> variable length argument list, multiple items of same type (... in java)
                Log.i(TAG, "doInBackground called: starts with ${url[0]}") // print first passed param here, (accessed as string array)

                val rssFeed = downloadXML(url[0]) // pass url -> first param
                if(rssFeed.isEmpty()) {
                    Log.e(TAG, "doInBackground: Error downloading")
                }
                return rssFeed
            }

            override fun onPostExecute(result: String) { // runs on main thread
                super.onPostExecute(result)
                Log.i(TAG, "onPostExecute called") // $result") // result is returned value of doInBackground
                // parse xml
                val parseXML = XMLParser()
                parseXML.parse(result)
                // set data to listview & custom adapter here
                val feedAdapter = FeedAdapter(dContext, R.layout.list_item, parseXML.getEntryList()) // companion objects cannot refer containing class
                dListView.adapter = feedAdapter
            }

            // http conn to read stream of data over net from url -> input stream reader
            // buffered reader -> stream from slow source/device (program reads data from buffer); reads chars, not strings
            private fun downloadXML(urlPath: String?): String {
                return URL(urlPath).readText() // reads URL content as string, not for huge files
                // ^ kotlin library's extension fn can be attached to existing classes
                    // readText() to URL class


                // replacing all the below code with above line
                /*
                val xmlResult = StringBuilder() // efficient for appends
                try {
                    val url = URL(urlPath)
                    val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
                    val response = connection.responseCode
                    Log.d(TAG, "downloadXML: response code is $response")

//            val inputStream = connection.inputStream
//            val inputStreamReader = InputStreamReader(inputStream)
//            val reader = BufferedReader(inputStreamReader)

//                    val reader = BufferedReader(InputStreamReader(connection.inputStream)) // here, closing buff_reader auto close the other 2: ISReader & IStream
//                    // read chars
//                    val inputBuffer = CharArray(500)
//                    var charsRead = 0
//                    while(charsRead >= 0) {
//                        charsRead = reader.read(inputBuffer) // read from buffer into char array
//                        // loop terminates when read()=0, no data left
//                        if(charsRead > 0) {
//                            xmlResult.append(String(inputBuffer, 0, charsRead))
//                        }
//                    }
//                    reader.close()
                    // above code replaced by idiomatic kotlin

//                    val stream = connection.inputStream
                        // use functional style, stream -> buffer -> reader to read it -> append to result
                    connection.inputStream.buffered().reader().use{  // param of type ISR, to read text & append
                        // also explicit param reader is not req
                        // this lambda fn executes the fn then closes it as well, also handles exceptions thrown
                        xmlResult.append(it.readText())
                    }

                    Log.d(TAG, "downloadXML: Received ${xmlResult.length} bytes")
                    return xmlResult.toString()
                }
//                catch (e: MalformedURLException) { // subclass of IOException
//                    Log.e(TAG, "downloadXML: Invalid URL ${e.message}")
//                    e.printStackTrace()
//                } catch (f: IOException) {
//                    Log.e(TAG, "downloadXML: IO Exception reading data ${f.message}")
//                    f.printStackTrace()
//                } catch (e: SecurityException) {
//                    Log.e(TAG, "downloadXML: Security exception ${e.message}") // Missing Internet permission comes under SecurityException
//                    e.printStackTrace()
//                } catch (g: Exception) {
//                    Log.e(TAG, "downloadXML: Unknown Error ${g.message}")
//                    g.printStackTrace()
//                }
                catch (e: Exception) {
                    val errorMessage: String = when (e) { // test type exception
                        is MalformedURLException -> "downloadXML: Invalid URL ${e.message}"
                        is IOException -> "downloadXML: IO Exception reading data ${e.message}"
                        is SecurityException -> "downloadXML: Security exception ${e.message}"
                        else -> { e.printStackTrace()
                            "downloadXML: Unknown Error ${e.message}" // put the assignment statement last in the block (errorMessage = "...")
                             }
                    }
                }
                return "" // return empty string due to error if try block did not execute
                */
            }
        }
    }

}

/*
Inner class -> holds reference to an activity
Static nested class -> exists independently
 */