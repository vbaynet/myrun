package akio.apps.myrun.feature.activitydetail.impl

import akio.apps._base.Resource
import akio.apps.myrun.data.activity.ActivityRepository
import akio.apps.myrun.data.authentication.UserAuthenticationState
import akio.apps.myrun.data.recentplace.UserRecentPlaceRepository
import akio.apps.myrun.domain.recentplace.CreateActivityDisplayPlaceNameUsecase
import akio.apps.myrun.feature.activitydetail.ActivityDetailViewModel
import akio.apps.myrun.feature.usertimeline.model.Activity
import akio.apps.myrun.feature.usertimeline.model.ActivityModelMapper
import androidx.lifecycle.viewModelScope
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class ActivityDetailViewModelImpl @Inject constructor(
    private val params: Params,
    private val activityRepository: ActivityRepository,
    private val activityModelMapper: ActivityModelMapper,
    private val userRecentPlaceRepository: UserRecentPlaceRepository,
    private val userAuthenticationState: UserAuthenticationState,
    private val createActivityDisplayPlaceNameUsecase: CreateActivityDisplayPlaceNameUsecase
) : ActivityDetailViewModel() {

    private val activityDetailsMutableStateFlow: MutableStateFlow<Resource<Activity>> =
        MutableStateFlow(Resource.Loading())
    override val activityDetails: Flow<Resource<Activity>> = activityDetailsMutableStateFlow

    override suspend fun getActivityPlaceDisplayName(): String? {
        val userId = userAuthenticationState.getUserAccountId() ?: return null
        val activityPlaceIdentifier =
            activityDetailsMutableStateFlow.value.data?.placeIdentifier ?: return null
        val currentUserPlaceIdentifier =
            userRecentPlaceRepository.getRecentPlaceIdentifier(userId)
        return createActivityDisplayPlaceNameUsecase(
            activityPlaceIdentifier,
            currentUserPlaceIdentifier
        )
    }

    override fun loadActivityDetails() {
        viewModelScope.launch {
            activityDetailsMutableStateFlow.value = Resource.Loading()
            val activity = activityRepository.getActivity(params.activityId)
                ?.let(activityModelMapper::map)
            if (activity == null) {
                activityDetailsMutableStateFlow.value = Resource.Error(ActivityNotFoundException())
            } else {
                activityDetailsMutableStateFlow.value = Resource.Success(activity)
            }
        }
    }
}
