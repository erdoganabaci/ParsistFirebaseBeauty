package com.example.erdo.parsistapps

import android.provider.ContactsContract
import android.support.v7.widget.DialogTitle
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem

class MyItem : ClusterItem{
    private var mPosition: LatLng? = null
    private var mTitle: String?=null
    private var mSnippet: String?=null

    constructor(location: LatLng?) {
        this.mPosition = location
    }
    constructor(location: LatLng?,mTitle: String,mSnippet : String){
        this.mPosition = location
        this.mTitle = mTitle
        this.mSnippet = mSnippet
    }

    override fun getSnippet(): String? {
            return this.mSnippet
    }

    override fun getTitle(): String? {
            return this!!.mTitle
    }

    override fun getPosition(): LatLng? {
        return mPosition
    }


}