package com.prakriti.top10downloader

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

// viewholder
class FeedViewHolder(view: View) {
    // store refs in VH, fVBId() is only called for new views to be inflated
    private val txtName: TextView = view.findViewById(R.id.txtName)
    private val txtArtist: TextView = view.findViewById(R.id.txtArtist)
    private val txtSummary: TextView = view.findViewById(R.id.txtSummary)

    fun getTxtName(): TextView {
        return txtName
    }
    fun getTxtArtist(): TextView {
        return txtArtist
    }
    fun getTxtSummary(): TextView {
        return txtSummary
    }
}

class FeedAdapter(context: Context, private val resLayout: Int, private val appFeedEntryList : List<FeedEntry>)
            : ArrayAdapter<FeedEntry>(context, resLayout) { // constructor

    private val TAG = "FeedAdapter"
    private val inflator = LayoutInflater.from(context)

    // inflate xml resource
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View { // inflates a new view each time its called
//        Log.d(TAG, "getView called")
        // convertView -> reusable view passed by the listview, its a view that is not appearing on screen anymore
        val view: View
        val viewHolder: FeedViewHolder

        if(convertView == null) { // logs to see how recycling of views happens
//            Log.d(TAG, "getView: convertView is null")
            view = inflator.inflate(resLayout, parent, false)
            viewHolder = FeedViewHolder (view) // new viewHolder
            view.tag = viewHolder // save it in view's tag
            // tag is of type object
        } else {
//            Log.d(TAG, "getView: convertView provided")
            view = convertView
            viewHolder = view.tag as FeedViewHolder // retrieve viewHolder
        }

        // use viewholder pattern -> hold/store views
        viewHolder.getTxtName().text = appFeedEntryList[position].getName()
        viewHolder.getTxtArtist().text = appFeedEntryList[position].getArtist()
        viewHolder.getTxtSummary().text = appFeedEntryList[position].getSummary()

        return view
    }

    override fun getCount(): Int {
//        Log.d(TAG, "getCount called")
        return appFeedEntryList.size
    }
}