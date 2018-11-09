package com.example.erdo.parsistapps

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker


class CustomInfoWindowAdapter(private val mContext: Context) : GoogleMap.InfoWindowAdapter {

    private val mWindow: View

    init {
        mWindow = LayoutInflater.from(mContext).inflate(R.layout.custom_info_window, null)

    }

    private fun rendowWindowText(marker: Marker, view: View) {

        val title = marker.title
        val tvTitle = view.findViewById<View>(R.id.title) as TextView

        if (title != "") {
            tvTitle.text = title
        }

        val snippet = marker.snippet
        val tvSnippet = view.findViewById(R.id.snippet) as TextView

        if (snippet != "") {
            tvSnippet.text = snippet
        }
    }

    override fun getInfoWindow(marker: Marker): View {
        rendowWindowText(marker, mWindow)
        return mWindow
    }

    override fun getInfoContents(marker: Marker): View {
        rendowWindowText(marker, mWindow)
        return mWindow
    }
}