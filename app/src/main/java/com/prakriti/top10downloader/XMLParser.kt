package com.prakriti.top10downloader

import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory

private const val TAG = "XMLParser"

class XMLParser {

    private val appFeedEntries = ArrayList<FeedEntry>()

    fun parse(xmlData: String): Boolean {
//        Log.d(TAG, "parse called with $xmlData") -> huge log
        var status = true
        var inEntry = false // to get values from tags in <entry> tag, not outside
        var textValue = ""

        try {
            // use java libraries to parse xml
            val factory = XmlPullParserFactory.newInstance() // discards stream once processed
            factory.isNamespaceAware = true
            val xpp = factory.newPullParser()
            xpp.setInput(xmlData.reader()) // String reader treats string as a stream, reader() is an extension fn
            var eventType = xpp.eventType
            var currentRecord = FeedEntry()

            while (eventType != XmlPullParser.END_DOCUMENT) { // event
                // check for tags we want
                val tagName = xpp.name?.toLowerCase()
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        //Log.d(TAG, "parse: Starting tag for $tagName")
                        if(tagName == "entry") {
                            inEntry = true
                        }
                    }
                    XmlPullParser.TEXT -> textValue = xpp.text
                    XmlPullParser.END_TAG -> {
                        //Log.d(TAG, "parse: Ending tag for $tagName")
                        if(inEntry) {
                            when (tagName) {
                                "entry" -> {
                                    appFeedEntries.add(currentRecord) // create new record to store currently held text values
                                    inEntry = false // since this is end tag
                                    currentRecord = FeedEntry() // creating new obj here
                                }
                                "name" -> currentRecord.setName(textValue)
                                "artist" -> currentRecord.setArtist(textValue)
                                "releasedate" -> currentRecord.setReleaseDate(textValue) // all converted to lowercase
                                "summary" -> currentRecord.setSummary(textValue)
                                "image" -> currentRecord.setImageURL(textValue)
                            }
                        }
                    }
                }
                // nothing else to do
                eventType = xpp.next()
            }
            // print out all entries to check
//            for (item in appFeedEntries) {
//                Log.d(TAG, "\n********************************************************************************************************\n")
//                Log.d(TAG, "parse: ${item.toString()}")
//            }
        } catch (e: Exception) {
            e.printStackTrace()
            status = false
        }
        return status
    }

    fun getEntryList() : ArrayList<FeedEntry> {
        return appFeedEntries
    }
}