package akio.apps.myrun.feature.feed.ui

import akio.apps.myrun.data.activity.api.model.BaseActivityModel
import akio.apps.myrun.data.user.api.model.MeasureSystem
import akio.apps.myrun.feature.activity.R
import akio.apps.myrun.feature.core.ktx.px2dp
import akio.apps.myrun.feature.core.ktx.rememberViewModelProvider
import akio.apps.myrun.feature.core.ui.AppColors
import akio.apps.myrun.feature.core.ui.AppDimensions
import akio.apps.myrun.feature.core.ui.ErrorDialog
import akio.apps.myrun.feature.feed.ActivityFeedViewModel
import akio.apps.myrun.feature.feed.FeedViewModel
import akio.apps.myrun.feature.feed.di.DaggerActivityFeedFeatureComponent
import akio.apps.myrun.feature.feed.di.DaggerFeedFeatureComponent
import akio.apps.myrun.feature.feed.model.FeedActivity
import akio.apps.myrun.feature.feed.model.FeedUiModel
import akio.apps.myrun.feature.feed.model.FeedUserFollowSuggestionList
import akio.apps.myrun.feature.feed.ui.ActivityFeedColors.listBackground
import akio.apps.myrun.feature.feed.ui.ActivityFeedDimensions.activityItemVerticalMargin
import android.app.Application
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import kotlin.math.roundToInt

private object ActivityFeedColors {
    val listBackground: Color = Color.White
}

internal object ActivityFeedDimensions {
    val activityItemVerticalMargin: Dp = 12.dp
    val activityItemHorizontalPadding: Dp = 16.dp
    val feedItemVerticalPadding: Dp = 12.dp
    val feedItemHorizontalPadding: Dp = 12.dp
}

@Composable
fun ActivityFeedComposable(
    appNavController: NavController,
    homeTabNavController: NavController,
    navEntry: NavBackStackEntry,
    contentPaddingBottom: Dp,
    onClickExportActivityFile: (BaseActivityModel) -> Unit,
) {
    val application = LocalContext.current.applicationContext as Application
    val activityFeedViewModel = navEntry.rememberViewModelProvider {
        DaggerActivityFeedFeatureComponent.factory().create(application).feedViewModel()
    }
    val feedViewModel = rememberViewModel()
    val uiState = rememberUiState(contentPaddingBottom)
    val navigator = rememberNavigator(appNavController, homeTabNavController)
    ActivityFeedComposableInternal(
        feedViewModel,
        activityFeedViewModel,
        uiState,
        navigator,
        onClickExportActivityFile
    )
}

@Composable
private fun rememberViewModel(): FeedViewModel {
    val application = LocalContext.current.applicationContext as Application
    return remember {
        DaggerFeedFeatureComponent.factory().create(application).feedViewModel()
    }
}

@Composable
private fun rememberUiState(contentPaddingBottom: Dp): FeedUiState {
    val coroutineScope = rememberCoroutineScope()
    val systemBarsTopDp = WindowInsets.systemBars.getTop(LocalDensity.current).px2dp.dp
    val topBarHeightDp = AppDimensions.AppBarHeight + systemBarsTopDp
    val topBarOffsetYAnimatable = remember { Animatable(0f) }
    val topBarHeightPx = with(LocalDensity.current) { topBarHeightDp.roundToPx().toFloat() }
    val feedListState = rememberLazyListState()
    return remember(topBarHeightDp) {
        FeedUiState(
            contentPaddingBottom,
            coroutineScope,
            topBarHeightDp,
            topBarHeightPx,
            topBarOffsetYAnimatable,
            feedListState,
        )
    }
}

@Composable
private fun rememberNavigator(
    appNavController: NavController,
    homeTabNavController: NavController,
): FeedNavigator = remember(appNavController, homeTabNavController) {
    FeedNavigator(appNavController, homeTabNavController)
}

@Composable
private fun ActivityFeedComposableInternal(
    feedViewModel: FeedViewModel,
    activityFeedViewModel: ActivityFeedViewModel,
    uiState: FeedUiState,
    navigator: FeedNavigator,
    onClickExportActivityFile: (BaseActivityModel) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(uiState.nestedScrollConnection)
    ) {
        ActivityFeedContainer(
            feedViewModel,
            activityFeedViewModel,
            uiState,
            navigator,
            onClickExportActivityFile
        )

        ActivityFeedTopBar(
            uiState,
            feedViewModel,
            Modifier
                .height(uiState.topBarHeightDp)
                .align(Alignment.TopCenter)
                .offset { IntOffset(x = 0, y = uiState.topBarOffsetYAnimatable.value.roundToInt()) }
                .background(AppColors.primary),
//            onClickUploadCompleteBadge = {
////                uiState.animateScrollToFeedItem(pos = 0)
////                activityFeedViewModel.setUploadBadgeDismissed(true)
//            },
            onClickUserPreferencesButton = navigator::navigateUserPreferences
        )
    }

    val screenError by activityFeedViewModel.launchCatchingError.collectAsState(initial = null)
    if (screenError != null) {
        ErrorDialog(text = screenError?.message ?: "") {
            activityFeedViewModel.setLaunchCatchingError(null)
        }
    }
}

@Composable
private fun ActivityFeedContainer(
    feedViewModel: FeedViewModel,
    activityFeedViewModel: ActivityFeedViewModel,
    uiState: FeedUiState,
    navigator: FeedNavigator,
    onClickExportActivityFile: (BaseActivityModel) -> Unit,
) {
    val lazyPagingItems = activityFeedViewModel.myActivityList.collectAsLazyPagingItems()
    val isLoadingInitialData by activityFeedViewModel.isInitialLoading.collectAsState(false)
    when {
        isLoadingInitialData ||
                (
                        lazyPagingItems.loadState.refresh == LoadState.Loading &&
                                lazyPagingItems.itemCount == 0
                        ) -> {
            FullscreenLoadingView()
        }
        lazyPagingItems.loadState.append.endOfPaginationReached &&
                lazyPagingItems.itemCount == 0 -> {
            ActivityFeedEmptyMessage(
                Modifier.padding(bottom = uiState.contentPaddings.calculateBottomPadding() + 8.dp)
            )
        }
        else -> ActivityFeedItemList(
            feedViewModel,
            activityFeedViewModel,
            uiState,
            navigator,
            lazyPagingItems,
            onClickExportActivityFile
        )
    }
}

@Composable
private fun ActivityFeedItemList(
    feedViewModel: FeedViewModel,
    activityFeedViewModel: ActivityFeedViewModel,
    uiState: FeedUiState,
    navigator: FeedNavigator,
    lazyPagingItems: LazyPagingItems<FeedUiModel>,
    onClickExportActivityFile: (BaseActivityModel) -> Unit,
) {
    val userProfile by activityFeedViewModel.userProfile.collectAsState(initial = null)
    val preferredUnitSystem by activityFeedViewModel.preferredSystem.collectAsState(
        initial = MeasureSystem.Default
    )
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(listBackground),
        contentPadding = uiState.contentPaddings,
        state = uiState.feedListState
    ) {
        items(
            lazyPagingItems,
            key = { feedItem -> feedItem.id }
        ) { feedItem ->
            when (feedItem) {
                is FeedActivity -> {
                    FeedActivityItem(
                        activityFeedViewModel,
                        feedItem,
                        userProfile,
                        preferredUnitSystem,
                        navigator,
                        onClickExportActivityFile
                    )
                }
                is FeedUserFollowSuggestionList -> {
                    FeedUserFollowSuggestionItem(
                        feedItem,
                        activityFeedViewModel::followUser,
                        navigator::navigateNormalUserStats
                    )
                }
                null -> {
                    // do nothing
                }
            }
        }

        if (lazyPagingItems.loadState.append == LoadState.Loading) {
            item { LoadingItem() }
        }
    }
}

@Composable
private fun ActivityFeedEmptyMessage(modifier: Modifier = Modifier) = Box(
    modifier = modifier
        .fillMaxWidth()
        .fillMaxHeight()
) {
    Text(
        text = stringResource(R.string.splash_welcome_text),
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .padding(horizontal = dimensionResource(R.dimen.common_page_horizontal_padding)),
        color = colorResource(R.color.activity_feed_instruction_text),
        fontSize = 30.sp,
        fontStyle = FontStyle.Italic,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center
    )
}

@Composable
private fun FullscreenLoadingView() = Box(
    modifier = Modifier.fillMaxSize(),
    contentAlignment = Alignment.Center
) {
    LoadingItem()
}

@Composable
private fun LoadingItem() = Column(
    modifier = Modifier
        .padding(20.dp)
        .fillMaxWidth(),
    horizontalAlignment = Alignment.CenterHorizontally
) {
    CircularProgressIndicator()
    Spacer(modifier = Modifier.height(10.dp))
    Text(
        text = stringResource(id = R.string.activity_feed_loading_item_message),
        color = colorResource(id = R.color.activity_feed_instruction_text),
        fontSize = 15.sp
    )
}

@Composable
internal fun FeedItem(content: @Composable () -> Unit) = Box(
    modifier = Modifier.padding(vertical = activityItemVerticalMargin)
) {
    Surface(
        elevation = 2.dp,
        content = content
    )
}

@Preview
@Composable
private fun LoadingItemPreview() = LoadingItem()
