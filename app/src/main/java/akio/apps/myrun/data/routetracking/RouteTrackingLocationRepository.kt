package akio.apps.myrun.data.routetracking

import akio.apps.myrun.data.routetracking.dto.TrackingLocationEntity
import android.location.Location
import kotlinx.coroutines.flow.Flow

interface RouteTrackingLocationRepository {
    suspend fun insert(trackingLocations: List<Location>)
    suspend fun clearRouteTrackingLocation()
    suspend fun getRouteTrackingLocationUpdates(skip: Int): List<TrackingLocationEntity>
    suspend fun getLatestLocationTime(): Long
    suspend fun getAllLocations(): List<TrackingLocationEntity>
}