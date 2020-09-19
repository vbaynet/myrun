package akio.apps.myrun.feature.routetracking.impl

import akio.apps.myrun.data.routetracking.RouteTrackingLocationRepository
import akio.apps.myrun.data.routetracking.RouteTrackingState
import akio.apps.myrun.data.routetracking.model.TrackingLocationEntity
import akio.apps.myrun.data.workout.WorkoutRepository
import akio.apps.myrun.data.workout.model.ActivityType
import akio.apps.myrun.data.workout.model.RunningWorkoutEntity
import akio.apps.myrun.data.workout.model.WorkoutEntity
import akio.apps.myrun.data.workout.model.WorkoutDataEntity
import akio.apps.myrun.feature._base.utils.GmsLatLng
import akio.apps.myrun.feature.routetracking.SaveRouteTrackingWorkoutUsecase
import android.graphics.Bitmap
import com.google.maps.android.PolyUtil
import javax.inject.Inject

class SaveRouteTrackingWorkoutWorkoutImpl @Inject constructor(
    private val routeTrackingState: RouteTrackingState,
    private val routeTrackingLocationRepository: RouteTrackingLocationRepository,
    private val workoutRepository: WorkoutRepository
) : SaveRouteTrackingWorkoutUsecase {

    override suspend fun saveCurrentWorkout(activityType: ActivityType, routeMapImage: Bitmap) {
        val endTime = System.currentTimeMillis()
        val startTime = routeTrackingState.getTrackingStartTime()
        val duration = routeTrackingState.getTrackingDuration()
        val workoutData: WorkoutEntity = WorkoutDataEntity("", activityType, startTime, endTime, duration)

        val savingWorkout = when (activityType) {
            ActivityType.Running -> {
                val distance = routeTrackingState.getRouteDistance()
                val averagePace = (duration / (1000 * 60)) / (distance / 1000)
                val encodedLocations = PolyUtil.encode(routeTrackingLocationRepository.getAllLocations().map { it.toGmsLatLng() })

                RunningWorkoutEntity(
                    workoutData = workoutData,
                    averagePace = averagePace,
                    distance = distance,
                    encodedPolyline = encodedLocations,
                    routePhoto = "",
                )
            }

            else -> return  // stop saving for unknown type
        }

        workoutRepository.saveWorkout(savingWorkout, routeMapImage)
    }

    private fun TrackingLocationEntity.toGmsLatLng() = GmsLatLng(latitude, longitude)
}