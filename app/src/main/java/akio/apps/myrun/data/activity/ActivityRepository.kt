package akio.apps.myrun.data.activity

import akio.apps.myrun.data.fitness.SingleDataPoint
import akio.apps.myrun.data.location.LocationEntity
import android.graphics.Bitmap

interface ActivityRepository {
    suspend fun getActivitiesByStartTime(userIds: List<String>, startAfterTime: Long, limit: Int): List<ActivityEntity>
    suspend fun saveActivity(
        activity: ActivityEntity,
        routeMapImage: Bitmap,
        speedDataPoints: List<SingleDataPoint<Float>>,
        stepCadenceDataPoints: List<SingleDataPoint<Int>>?,
        locationDataPoints: List<SingleDataPoint<LocationEntity>>
    ): String
}