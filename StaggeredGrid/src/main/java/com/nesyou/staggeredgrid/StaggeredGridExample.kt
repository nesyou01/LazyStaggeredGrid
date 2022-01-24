package com.nesyou.staggeredgrid

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

@Composable
private fun StaggeredGridExample() {
    LazyStaggeredGrid(
        /** Two fixed cells */
        cells = StaggeredCells.Fixed(2),


        /** 20 Dp of padding horizontally */
        contentPadding = PaddingValues(horizontal = 20.dp),


        /** The content */
        content = {
            items(20) {
                Text("Items number $it")
            }
        }
    )
}