package akio.apps.myrun.feature.routetracking.view

import akio.apps.myrun.R
import akio.apps.myrun._base.utils.StatsPresentations
import akio.apps.myrun.data.activity.api.model.ActivityType
import akio.apps.myrun.databinding.MergeRouteTrackingStatsViewBinding
import akio.apps.myrun.feature.routetracking.impl.RouteTrackingStats
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout

class RouteTrackingStatsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val viewBinding: MergeRouteTrackingStatsViewBinding =
        MergeRouteTrackingStatsViewBinding.inflate(LayoutInflater.from(context), this)

    private var speedPresenter: ((Double) -> String)? = null

    private val activityTypes = listOf(ActivityType.Running, ActivityType.Cycling)

    init {
        readAttrs(attrs)
    }

    private fun readAttrs(attrs: AttributeSet?) {
        attrs ?: return

        val styledAttrs = context.obtainStyledAttributes(attrs, R.styleable.RouteTrackingStatsView)
        val activityTypeIndex =
            styledAttrs.getInteger(R.styleable.RouteTrackingStatsView_activityTypeIndex, 0)
        styledAttrs.recycle()

        setActivityType(activityTypes[activityTypeIndex])
    }

    fun setActivityType(activityType: akio.apps.myrun.data.activity.api.model.ActivityType) {
        when (activityType) {
            ActivityType.Running -> {
                viewBinding.speedLabelTextView.setText(R.string.route_tracking_pace_label)
                viewBinding.speedUnitTextView.setText(R.string.performance_unit_pace_min_per_km)
                speedPresenter = { speed -> StatsPresentations.getDisplayPace(speed) }
            }
            ActivityType.Cycling -> {
                viewBinding.speedLabelTextView.setText(R.string.route_tracking_speed_label)
                viewBinding.speedUnitTextView.setText(R.string.common_speed_unit)
                speedPresenter = { speed -> StatsPresentations.getDisplaySpeed(speed) }
            }
            else -> throw Exception("INvalid activity type")
        }
    }

    fun update(stats: RouteTrackingStats) = viewBinding.apply {
        distanceTextView.text = StatsPresentations.getDisplayTrackingDistance(stats.distance)
        timeTextView.text = StatsPresentations.getDisplayDuration(stats.duration)
        speedTextView.text = speedPresenter?.invoke(stats.speed)
    }
}
