package com.nesyou.staggeredgrid


import androidx.compose.animation.core.AnimationState
import androidx.compose.animation.core.animateDecay
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.*
import kotlin.math.abs

/**
 * The composable of staggered grid
 *
 * @param modifier the modifier
 * @param contentPadding the padding that will surround the content
 * @param cells how the content will be visible
 * @param state the state of the first column in the [LazyStaggeredGrid]
 * @param content the content, must be of [StaggeredGridScope]
 *
 * @author Younes Lagmah
 * @since 2022.01.24
 * @sample com.nesyou.staggeredgrid.StaggeredGridExample()
 */
@Composable
fun LazyStaggeredGrid(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    cells: StaggeredCells,
    state: LazyListState = rememberLazyListState(),
    content: StaggeredGridScope.() -> Unit,
) {
    val scope = StaggeredGridScopeImpl()
    scope.apply(content)
    BoxWithConstraints(
        modifier = modifier
    ) {
        StaggeredGrid(
            scope = scope,
            padding = contentPadding,
            columnsNumber = if (cells is StaggeredCells.Fixed) cells.count else maxOf(
                (maxWidth / (cells as StaggeredCells.Adaptive).minSize).toInt(),
                1
            ),
            state = state,
        )
    }
}


@Composable
internal fun StaggeredGrid(
    scope: StaggeredGridScopeImpl,
    padding: PaddingValues,
    columnsNumber: Int,
    state: LazyListState,
) {
    val states = mutableListOf(state).apply {
        repeat(columnsNumber - 1) { this.add(element = rememberLazyListState()) }
    }
    val layoutDirection = LocalLayoutDirection.current
    val coroutineScope = rememberCoroutineScope()
    val flingDecay = rememberSplineBasedDecay<Float>()
    suspend fun flingBehavior(
        i: Float,
        state: ScrollableState,
    ) {
        if (abs(i) > 1f) {
            var velocityLeft = i
            var lastValue = 0f
            AnimationState(
                initialValue = 0f,
                initialVelocity = i,
            ).animateDecay(flingDecay) {
                val delta = value - lastValue
                lastValue = value
                velocityLeft = this.velocity
                val c = state.dispatchRawDelta(delta)
                if (abs(delta - c) > 0.5f) this.cancelAnimation()
            }
            state.scrollBy(velocityLeft)
        } else {
            state.scrollBy(i)
        }
    }

    val nestedScroll = object : NestedScrollConnection {
        override suspend fun onPreFling(available: Velocity): Velocity {
            coroutineScope.launch {
                joinAll(*states.map { launch { flingBehavior(-available.y, it) } }.toTypedArray())
            }
            return super.onPreFling(available)
        }
        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
            states.forEach {
                it.dispatchRawDelta(-available.y)
            }
            return super.onPreScroll(available, source)
        }
    }

    Row(
        Modifier.nestedScroll(nestedScroll)
    ) {
        repeat(columnsNumber) {
            LazyColumn(
                modifier = Modifier
                    .weight(1F)
                    .disabledVerticalPointerInputScroll(),
                state = states[it],
                contentPadding = PaddingValues(
                    start = if (it == 0) padding.calculateLeftPadding(layoutDirection) else 0.dp,
                    end = if (it == columnsNumber - 1) padding.calculateRightPadding(layoutDirection) else 0.dp,
                    top = padding.calculateTopPadding(),
                    bottom = padding.calculateBottomPadding()
                )
            ) {
                for (i in scope.content.indices step columnsNumber) {
                    if (scope.content.size > i + it) {
                        item {
                            scope.content[i + it]()
                        }
                    }
                }
            }
        }
    }

}


sealed class StaggeredCells {
    /**
     * Combines cells with a fixed number of columns.
     */
    class Fixed(val count: Int) : StaggeredCells()

    /**
     * Combine cells with an adaptive number of columns depends on each screen with the given [minSize]
     */
    class Adaptive(val minSize: Dp) : StaggeredCells()
}


class StaggeredGridScopeImpl : StaggeredGridScope {
    private val _data = mutableListOf<@Composable () -> Unit>()

    val content get() = _data.toList()

    override fun item(content: @Composable () -> Unit) {
        _data.add(content)
    }

    override fun items(count: Int, itemContent: @Composable (index: Int) -> Unit) {
        repeat(count) {
            _data.add {
                itemContent(it)
            }
        }
    }

    override fun <T> items(items: Array<T>, itemContent: @Composable (item: T) -> Unit) {
        items.forEach {
            _data.add {
                itemContent(it)
            }
        }
    }


    override fun <T> items(items: List<T>, itemContent: @Composable (item: T) -> Unit) {
        items.forEach {
            _data.add {
                itemContent(it)
            }
        }
    }
}

interface StaggeredGridScope {
    /**
     * Add a single items
     *
     * @param content the content
     */
    fun item(content: @Composable () -> Unit)


    /**
     * Add an item that will be repeated [count] times
     *
     * @param count count of times
     * @param itemContent items content
     */
    fun items(count: Int, itemContent: @Composable (index: Int) -> Unit)


    /**
     * Add an array of items
     *
     * @param items items array
     * @param itemContent items content
     */
    fun <T> items(items: Array<T>, itemContent: @Composable (item: T) -> Unit)


    /**
     * Add an list of items
     *
     * @param items items list
     * @param itemContent items content
     */
    fun <T> items(items: List<T>, itemContent: @Composable (item: T) -> Unit)
}

internal fun Modifier.disabledVerticalPointerInputScroll() =
    this.nestedScroll(VerticalScrollConsumer)

internal val VerticalScrollConsumer = object : NestedScrollConnection {
    override fun onPreScroll(available: Offset, source: NestedScrollSource) =
        available.copy(x = 0f)

    override suspend fun onPreFling(available: Velocity) = available.copy(x = 0f)
}