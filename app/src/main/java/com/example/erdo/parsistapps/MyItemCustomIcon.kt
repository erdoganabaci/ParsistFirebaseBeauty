package com.example.erdo.parsistapps

import android.content.Context
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer

class MyItemCustomIcon : DefaultClusterRenderer<MyItem> {
    private var mContext:Context?=null

    constructor(context: Context?, map: GoogleMap?, clusterManager: ClusterManager<MyItem>?) : super(context, map, clusterManager) {
        mContext=context
    }


    override fun onBeforeClusterRendered(cluster: Cluster<MyItem>?, markerOptions: MarkerOptions?) {
        super.onBeforeClusterRendered(cluster, markerOptions)
    }

    override fun onBeforeClusterItemRendered(item: MyItem?, markerOptions: MarkerOptions?) {
        markerOptions?.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher_park))
    }

    override fun shouldRenderAsCluster(cluster: Cluster<MyItem>?): Boolean {
        return cluster!!.size >1
    }
}