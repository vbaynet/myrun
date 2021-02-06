package akio.apps.myrun._di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import dagger.android.AndroidInjector
import javax.inject.Inject

class ViewModelInjectionDelegate(
    androidInjector: AndroidInjector<Any>,
    private val viewModelStoreOwner: ViewModelStoreOwner
) {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    val viewModelProvider by lazy { ViewModelProvider(viewModelStoreOwner, viewModelFactory) }

    init {
        androidInjector.inject(this)
    }

    inline fun <reified T : ViewModel> getViewModel(): T = viewModelProvider[T::class.java]
}
