package akio.apps.myrun.feature.activitydetail

import akio.apps.myrun.data.activity.wiring.ActivityDataComponent
import akio.apps.myrun.data.activity.wiring.DaggerActivityDataComponent
import akio.apps.myrun.data.authentication.wiring.AuthenticationDataComponent
import akio.apps.myrun.data.authentication.wiring.DaggerAuthenticationDataComponent
import akio.apps.myrun.data.user.wiring.DaggerUserDataComponent
import akio.apps.myrun.data.user.wiring.UserDataComponent
import akio.apps.myrun.wiring.domain.DaggerDomainComponent
import akio.apps.myrun.wiring.domain.DomainComponent
import androidx.lifecycle.SavedStateHandle
import dagger.BindsInstance
import dagger.Component

@Component(
    dependencies = [
        ActivityDataComponent::class,
        AuthenticationDataComponent::class,
        UserDataComponent::class,
        DomainComponent::class
    ]
)
interface ActivityDetailFeatureComponent {
    fun activityDetailsViewModel(): ActivityDetailViewModel

    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance savedStateHandle: SavedStateHandle,
            activityDataComponent: ActivityDataComponent =
                DaggerActivityDataComponent.create(),
            authenticationDataComponent: AuthenticationDataComponent =
                DaggerAuthenticationDataComponent.create(),
            userDataComponent: UserDataComponent = DaggerUserDataComponent.create(),
            domainComponent: DomainComponent = DaggerDomainComponent.factory().create(),
        ): ActivityDetailFeatureComponent
    }
}
