package com.nesyou.staggeredgrid


import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.*

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

    val scroll = rememberScrollableState { delta ->
        coroutineScope.launch { states.forEach { it.scrollBy(-delta) } }
        delta
    }

    Row(
        modifier = Modifier.scrollable(
            scroll,
            Orientation.Vertical,
            flingBehavior = ScrollableDefaults.flingBehavior()
        )
    ) {
        repeat(columnsNumber) {
            LazyColumn(
                modifier = Modifier
                    .weight(1F),
                state = states[it],
                contentPadding = PaddingValues(
                    start = if (it == 0) padding.calculateLeftPadding(layoutDirection) else 0.dp,
                    end = if (it == columnsNumber - 1) padding.calculateRightPadding(
                        layoutDirection
                    ) else 0.dp,
                    top = padding.calculateTopPadding(),
                    bottom = padding.calculateBottomPadding()
                ),
                userScrollEnabled = false
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
