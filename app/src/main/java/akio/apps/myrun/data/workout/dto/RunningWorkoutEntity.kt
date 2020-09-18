package akio.apps.myrun.data.workout.dto

data class RunningWorkoutEntity(
    val workoutData: WorkoutEntity,

    // info
    val routePhoto: String,

    // stats
    val averagePace: Double,
    val distance: Double,

    // data points
    val encodedPolyline: String
): WorkoutEntity by workoutData