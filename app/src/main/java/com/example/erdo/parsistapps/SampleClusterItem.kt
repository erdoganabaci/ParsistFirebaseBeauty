package com.example.erdo.parsistapps

import android.support.annotation.NonNull
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem


class SampleClusterItem: ClusterItem {
    override fun getPosition(): LatLng? {
        return this!!.location
    }

    private var location: LatLng? = null
    constructor (@NonNull location:LatLng) {
        this.location = location
    }
    override fun getSnippet(): String? {
        return null
    }


    override fun getTitle(): String? {
        return null
    }
}