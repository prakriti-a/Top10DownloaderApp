package com.prakriti.top10downloader

import android.os.AsyncTask
import android.util.Log
import java.net.URL

// edited to use ViewModel -> no references to UI

// ******* one instance of ASYNC TASK can only be executed once ********
// inner class to access properties of this class -> not visible to any other class

private const val TAG = "DownloadData"

class DownloadData(private val callback: DownloaderCallback) : AsyncTask<String, Void, String>() { // no nullable type inputs
    // using a listener to get the caller of this download task
    // removed params -> context: Context, listView: ListView
    // async processing
    // string url, void progressBar, string result

  /*  private var dContext: Context by Delegates.notNull() // to mitigate memory leaks
    private var dListView: ListView by Delegates.notNull() // = listView

    // init block for properties
    init {
        dContext = context
        dListView = listView
    }
*/

    interface DownloaderCallback {
        fun onDataAvailable(data: List<FeedEntry>)
    }

    override fun doInBackground(vararg url: String): String { // performs all these tasks in separate thread
        // vararg -> variable length argument list, multiple items of same type (... in java)
        Log.i(TAG, "doInBackground called: starts with ${url[0]}") // print first passed param here, (accessed as string array)

        val rssFeed = downloadXML(url[0]) // pass url -> first param
        if(rssFeed.isEmpty()) {
            Log.e(TAG, "doInBackground: Error downloading")
        }
        return rssFeed
    }

    // tightly coupled with other classes
    // this class is meant specifically for downloading XML data that conforms to RSS specs, not other data
    override fun onPostExecute(result: String) { // runs on main thread
//        super.onPostExecute(result)
        Log.i(TAG, "onPostExecute called") // $result") // result is returned value of doInBackground
        // parse xml
        val parseXML = XMLParser()
        if(result.isNotEmpty()) {
            parseXML.parse(result)
        }
        callback.onDataAvailable(parseXML.getEntryList())

        // set data to listview & custom adapter here
        //val feedAdapter = FeedAdapter(dContext, R.layout.list_item, parseXML.getEntryList()) // companion objects cannot refer containing class
        //dListView.adapter = feedAdapter
    }

    // http conn to read stream of data over net from url -> input stream reader
    // buffered reader -> stream from slow source/device (program reads data from buffer); reads chars, not strings
    private fun downloadXML(urlPath: String): String {
        try {
            return URL(urlPath).readText() // reads URL content as string, not for huge files
            // ^ kotlin library's extension fn can be attached to existing classes
            // readText() to URL class
        }
        catch (e: Exception) { // here, can re-use earlier catch block
            Log.e(TAG, "downloadXML: ${e.message}")
            e.printStackTrace()
        }
        return "" // in case of exception

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