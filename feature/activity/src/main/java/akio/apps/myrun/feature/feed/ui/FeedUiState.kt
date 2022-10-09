package akio.apps.myrun.feature.feed.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.unit.Dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class FeedUiState(
    contentPaddingBottom: Dp,
    val coroutineScope: CoroutineScope,
    val topBarHeightDp: Dp,
    val topBarHeightPx: Float,
    val topBarOffsetYAnimatable: Animatable<Float, AnimationVector1D>,
    val feedListState: LazyListState,
    var uploadBadgeState: UploadBadgeState
) {
    val contentPaddings = PaddingValues(
        top = topBarHeightDp,
        bottom = contentPaddingBottom
    )

    val nestedScrollConnection = object : NestedScrollConnection {
        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
            val delta = available.y
            val targetOffset = when {
                delta >= REVEAL_ANIM_THRESHOLD -> 0f
                delta <= -REVEAL_ANIM_THRESHOLD -> -topBarHeightPx
                else -> return Offset.Zero
            }
            coroutineScope.launch {
                topBarOffsetYAnimatable.animateTo(targetOffset)
            }

            return Offset.Zero
        }
    }

    fun updateUploadBadgeState(activityUploadingCount: Int) {
        uploadBadgeState = when {
            activityUploadingCount > 0 -> UploadBadgeState.InProgress
            activityUploadingCount == 0 && uploadBadgeState != UploadBadgeState.Dismissed -> {
                UploadBadgeState.Complete
            }
            else -> UploadBadgeState.Dismissed
        }
    }

    fun dismissActivityUploadBadge() {
        coroutineScope.launch {
            feedListState.animateScrollToItem(0)
        }
        uploadBadgeState = UploadBadgeState.Dismissed
    }

    enum class UploadBadgeState {
        InProgress, Complete, Dismissed
    }

    companion object {
        private const val REVEAL_ANIM_THRESHOLD = 10
    }
}
