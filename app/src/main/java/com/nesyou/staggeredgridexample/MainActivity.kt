package com.nesyou.staggeredgridexample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.nesyou.staggeredgrid.StaggeredCells
import com.nesyou.staggeredgrid.LazyStaggeredGrid
import com.nesyou.staggeredgridexample.ui.theme.StaggeredGridExampleTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StaggeredGridExampleTheme {
                LazyStaggeredGrid(cells = StaggeredCells.Adaptive(minSize = 180.dp)) {
                    items(60) {
                        val random: Double = 100 + Math.random() * (500 - 100)
                        Image(
                            painter = painterResource(id = R.drawable.asd),
                            contentDescription = null,
                            modifier = Modifier.height(random.dp).padding(10.dp),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }
    }
}
