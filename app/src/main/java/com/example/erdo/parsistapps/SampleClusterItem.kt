package com.example.erdo.parsistapps

internal class SampleClusterItem(@param:NonNull private val location: LatLng) : ClusterItem {

    val latitude: Double
        get() = location.latitude

    val longitude: Double
        get() = location.longitude

    val title: String?
        @Nullable
        get() = null

    val snippet: String?
        @Nullable
        get() = null
}