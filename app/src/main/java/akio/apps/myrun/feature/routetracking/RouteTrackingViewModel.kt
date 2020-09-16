package akio.apps.myrun.feature.routetracking

import akio.apps.common.lifecycle.Event
import akio.apps.common.viewmodel.BaseViewModel
import android.location.Location
import androidx.lifecycle.LiveData

abstract class RouteTrackingViewModel : BaseViewModel() {
    abstract val mapInitialLocation: LiveData<Event<Location>>
    abstract fun requestMapInitialLocation()
}