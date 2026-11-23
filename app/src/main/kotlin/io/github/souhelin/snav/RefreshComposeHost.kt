package io.github.souhelin.snav

import android.content.Context
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

/** Compose hosts only the browsing surface; the rest of the app remains Views. */
@OptIn(ExperimentalMaterial3Api::class)
class RefreshComposeHost(
    context: Context,
    container: FrameLayout,
    browser: RefreshAwareGeckoView,
    onRefresh: Runnable,
) {
    private var refreshing by mutableStateOf(false)
    private val composeView = ComposeView(context)

    init {
        // The Material 3 pull modifier applies a 0.5 drag multiplier: a 55 dp
        // positional threshold therefore corresponds to a 110 dp finger drag.
        (browser.parent as? ViewGroup)?.removeView(browser)
        container.addView(composeView, 0, FrameLayout.LayoutParams(-1, -1))
        composeView.setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        composeView.setContent {
            val state = rememberPullToRefreshState()
            val threshold = 55.dp
            MaterialTheme {
                PullToRefreshBox(
                    isRefreshing = refreshing,
                    onRefresh = { onRefresh.run() },
                    modifier = Modifier.fillMaxSize(),
                    state = state,
                    threshold = threshold,
                    indicator = {
                        PullToRefreshDefaults.Indicator(
                            modifier = Modifier.align(Alignment.TopCenter),
                            isRefreshing = refreshing,
                            state = state,
                            threshold = threshold,
                            containerColor = Color(context.getColor(R.color.snav_surface)),
                            color = Color(context.getColor(R.color.snav_accent)),
                        )
                    },
                ) {
                    AndroidView(factory = { browser }, modifier = Modifier.fillMaxSize())
                }
            }
        }
    }

    fun setRefreshing(value: Boolean) { refreshing = value }
}
