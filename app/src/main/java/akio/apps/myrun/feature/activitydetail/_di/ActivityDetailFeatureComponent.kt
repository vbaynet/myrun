package akio.apps.myrun.feature.activitydetail._di

import akio.apps.common.wiring.DispatchersModule
import akio.apps.myrun.feature.activitydetail.impl.ActivityDetailViewModel
import akio.apps.myrun.wiring.data.activity.ActivityDataComponent
import akio.apps.myrun.wiring.data.activity.DaggerActivityDataComponent
import akio.apps.myrun.wiring.data.authentication.AuthenticationDataComponent
import akio.apps.myrun.wiring.data.authentication.DaggerAuthenticationDataComponent
import akio.apps.myrun.wiring.data.user.DaggerUserDataComponent
import akio.apps.myrun.wiring.data.user.UserDataComponent
import dagger.BindsInstance
import dagger.Component

@Component(
    modules = [
        DispatchersModule::class
    ],
    dependencies = [
        ActivityDataComponent::class,
        AuthenticationDataComponent::class,
        UserDataComponent::class
    ]
)
interface ActivityDetailFeatureComponent {
    fun activityDetailsViewModel(): ActivityDetailViewModel

    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance params: ActivityDetailViewModel.Params,
            activityDataComponent: ActivityDataComponent =
                DaggerActivityDataComponent.create(),
            authenticationDataComponent: AuthenticationDataComponent =
                DaggerAuthenticationDataComponent.create(),
            userDataComponent: UserDataComponent = DaggerUserDataComponent.create(),
        ): ActivityDetailFeatureComponent
    }
}
