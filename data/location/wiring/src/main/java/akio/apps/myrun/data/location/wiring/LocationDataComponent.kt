package akio.apps.myrun.data.location.wiring

import akio.apps.base.wiring.ApplicationModule
import akio.apps.base.wiring.DispatchersModule
import akio.apps.myrun.data.location.api.LocationDataSource
import akio.apps.myrun.data.location.api.PlaceDataSource
import dagger.Component

@Component(
    modules = [
        LocationDataModule::class,
        ApplicationModule::class,
        DispatchersModule::class
    ]
)
interface LocationDataComponent {
    fun placeDataSource(): PlaceDataSource
    fun locationDataSource(): LocationDataSource
}
