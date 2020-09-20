package akio.apps.myrun.feature._base.utils

import akio.apps.myrun.data.routetracking.TrackingLocationEntity
import android.location.Location
import com.google.android.gms.maps.model.LatLng

typealias GmsLatLng = LatLng

fun Location.toLatLng() = akio.apps.myrun.feature.routetracking.LatLng(latitude, longitude)
fun Location.toGmsLatLng() = GmsLatLng(latitude, longitude)
fun TrackingLocationEntity.toGmsLatLng() = GmsLatLng(latitude, longitude)
fun akio.apps.myrun.feature.routetracking.LatLng.toGmsLatLng() = GmsLatLng(latitude, longitude)