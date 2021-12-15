package akio.apps.myrun.feature.splash.wiring

import akio.apps.myrun.data.authentication.AuthenticationDataModule
import akio.apps.myrun.domain.launchcatching.LaunchCatchingModule
import akio.apps.myrun.feature.splash.SplashViewModel
import dagger.Component

@Component(
    modules = [
        LaunchCatchingModule::class,
        AuthenticationDataModule::class,
    ]
)
interface SplashFeatureComponent {
    fun splashViewModel(): SplashViewModel

    @Component.Factory
    interface Factory {
        fun create(): SplashFeatureComponent
    }
}
