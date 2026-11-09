package dev.medriid.davit.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.medriid.davit.data.ago
import dev.medriid.davit.design.DrumIndicator
import dev.medriid.davit.design.Gutter
import dev.medriid.davit.design.InkText
import dev.medriid.davit.design.LocalInks
import dev.medriid.davit.design.RisoType
import dev.medriid.davit.design.RouteHead
import dev.medriid.davit.design.rememberHaptics

/** Every screen above the tabs uses this: scrollable, with room for the top bar and home bar. */
@Composable
fun RouteScreen(
    route: Route,
    title: String,
    subtitle: String? = null,
    refreshing: Boolean = false,
    onRefresh: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val inks = LocalInks.current
    Box(Modifier.fillMaxSize().sharedCard(route.sharedKey).background(inks.paper)) {
        PullBox(refreshing, onRefresh, Modifier.fillMaxSize()) {
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(start = Gutter, end = Gutter, top = 4.dp, bottom = 28.dp),
            ) {
                RouteHead(title, subtitle)
                Spacer(Modifier.height(12.dp))
                content()
            }
        }
    }
}

/** [RouteScreen] for long lists (a course page can hold hundreds of files): only what's on screen is composed. */
@Composable
fun RouteLazyScreen(
    route: Route,
    title: String,
    subtitle: String? = null,
    refreshing: Boolean = false,
    onRefresh: (() -> Unit)? = null,
    content: LazyListScope.() -> Unit,
) {
    val inks = LocalInks.current
    Box(Modifier.fillMaxSize().sharedCard(route.sharedKey).background(inks.paper)) {
        PullBox(refreshing, onRefresh, Modifier.fillMaxSize()) {
            LazyColumn(
                Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = Gutter, end = Gutter, top = 4.dp, bottom = 28.dp),
            ) {
                item(key = "head") {
                    RouteHead(title, subtitle)
                    Spacer(Modifier.height(12.dp))
                }
                content()
            }
        }
    }
}

/** Pull to refresh with the riso drum; a plain box without [onRefresh]. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PullBox(refreshing: Boolean, onRefresh: (() -> Unit)?, modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    if (onRefresh == null) {
        Box(modifier, content = content)
        return
    }
    val state = rememberPullToRefreshState()
    val haptics = rememberHaptics()
    PullToRefreshBox(
        isRefreshing = refreshing,
        onRefresh = {
            haptics.heavy()
            onRefresh()
        },
        modifier = modifier,
        state = state,
        indicator = { DrumIndicator(state.distanceFraction, refreshing, Modifier.align(Alignment.TopCenter)) },
    ) {
        Box(
            Modifier.fillMaxSize().graphicsLayer {
                translationY = state.distanceFraction.coerceIn(0f, 1.4f) * 52.dp.toPx()
            },
            content = content,
        )
    }
}

@Composable
fun TabColumn(content: @Composable ColumnScope.() -> Unit) {
    val inks = LocalInks.current
    Column(
        Modifier
            .fillMaxSize()
            .background(inks.paper)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Gutter)
            .padding(top = 10.dp, bottom = 28.dp),
        content = content,
    )
}

@Composable
fun GroupLabel(text: String, modifier: Modifier = Modifier) {
    InkText(text, label = true, modifier = modifier.padding(top = 14.dp, bottom = 6.dp))
}

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    InkText(text, label = true, modifier = modifier.padding(top = 18.dp, bottom = 8.dp))
}

@Composable
fun Hairline(modifier: Modifier = Modifier) {
    val inks = LocalInks.current
    Spacer(modifier.fillMaxWidth().height(1.dp).background(inks.rule))
}

@Composable
fun Freshness(at: Long) {
    if (at <= 0L) return
    InkText("Updated ${ago(at)} · pull down to refresh", style = RisoType.small.copy(color = LocalInks.current.rule), modifier = Modifier.padding(bottom = 10.dp))
}

@Composable
fun ToggleRow(title: String, body: String, on: Boolean, onToggle: () -> Unit) {
    val inks = LocalInks.current
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(role = Role.Switch, onClick = onToggle)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            InkText(title, style = RisoType.body.copy(fontWeight = FontWeight.SemiBold))
            InkText(body, style = RisoType.small)
        }
        Spacer(Modifier.width(12.dp))
        Switch(
            checked = on,
            onCheckedChange = { onToggle() },
            colors = SwitchDefaults.colors(
                checkedThumbColor = inks.paper,
                checkedTrackColor = inks.a,
                uncheckedThumbColor = inks.rule,
                uncheckedTrackColor = inks.paper,
            ),
        )
    }
}
