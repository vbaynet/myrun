package akio.apps.myrun.feature.feed.model

import akio.apps.myrun.data.activity.api.model.BaseActivityModel
import akio.apps.myrun.data.user.api.model.UserFollow
import java.util.concurrent.atomic.AtomicLong

sealed class FeedUiModel(val id: String = "feed-auto-id-${ItemAutoId.incrementAndGet()}") {
    companion object {
        private val ItemAutoId: AtomicLong = AtomicLong()
    }
}

data class FeedActivity(val activityData: BaseActivityModel) : FeedUiModel(activityData.id)

data class FeedUserFollowSuggestion(val userList: List<UserFollow>) : FeedUiModel()
