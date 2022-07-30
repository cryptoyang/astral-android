package cc.cryptopunks.wrapdrive.offer.v2

import androidx.compose.foundation.layout.Column
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.TabRowDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import cc.cryptopunks.wrapdrive.api.FilterIn
import cc.cryptopunks.wrapdrive.api.FilterOut
import cc.cryptopunks.wrapdrive.offer.OfferModel
import cc.cryptopunks.wrapdrive.offer.subscribeChanges
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.pagerTabIndicatorOffset
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.launch
import org.w3c.dom.Text

@Preview
@Composable
fun DashboardPreview() = PreviewBox {
    DashboardView(PreviewModel.instance)
}


private val pages = listOf("received", "send")
private val filters = listOf(FilterIn, FilterOut)

@OptIn(ExperimentalPagerApi::class)
@Composable
fun DashboardView(
    model: OfferModel,
    navController: NavController = rememberNavController(),
) {
    LaunchedEffect(Unit) {
        model.subscribeChanges()
    }
    Column {
        val pagerState = rememberPagerState()
        val currentPage = pagerState.currentPage
        val scope = rememberCoroutineScope()
        LaunchedEffect(currentPage) { pagerState.animateScrollToPage(currentPage) }

        // Tabs
        TabRow(
            selectedTabIndex = currentPage,
            backgroundColor = MaterialTheme.colors.background,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    modifier = Modifier.pagerTabIndicatorOffset(pagerState, tabPositions)
                )
            }
        ) {
            pages.forEachIndexed { index, title ->
                Tab(
                    text = { Text(title.uppercase()) },
                    selected = pagerState.currentPage == index,
                    onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                )
            }
        }

        // Pages
        HorizontalPager(
            count = filters.size,
            state = pagerState
        ) { page ->
            val filter = filters[page]
            OfferItems(
                model = model,
                filter = filter,
                navController = navController
            )
        }
    }
}
