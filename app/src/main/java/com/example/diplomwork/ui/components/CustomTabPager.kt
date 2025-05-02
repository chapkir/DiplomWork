package com.example.diplomwork.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalPagerApi::class)
@Composable
fun CustomTabPager(
    tabTitles: List<String>,
    pagerState: PagerState,
    modifier: Modifier = Modifier,
    lineOffset: Double,
    tabContent: @Composable (page: Int) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val tabWidths = remember { mutableStateListOf<Float>() }

    // Вычисление смещения полоски с учетом свайпа
    val offset by remember {
        derivedStateOf {
            val pageOffset = pagerState.currentPage + pagerState.currentPageOffset
            val tabWidth = tabWidths.getOrNull(0) ?: 0f

            // Считаем смещение полоски на основе текущей страницы и её смещения
            (tabWidth * pageOffset).coerceIn(0f, tabWidth * (tabTitles.size - 1))
        }
    }

    Column(modifier = modifier) {
        Box {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .onGloballyPositioned { coordinates ->
                                if (tabWidths.size < tabTitles.size) {
                                    tabWidths.add(coordinates.size.width.toFloat())
                                }
                            }
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            }
                    ) {
                        Text(
                            text = title,
                            color = if (pagerState.currentPage == index) Color.White else Color.Gray,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                        )
                    }
                }
            }

            // Полоска индикатора, движущаяся параллельно свайпу
            if (tabWidths.size == tabTitles.size) {
                // Считываем ширину текущей вкладки
                val currentTabWidth = tabWidths.getOrNull(pagerState.currentPage) ?: 0f
                Box(
                    modifier = Modifier
                        .padding(top = 30.dp)
                        .offset {
                            // Сдвигаем полоску по горизонтали в зависимости от текущей страницы
                            IntOffset(
                                offset.roundToInt() + ((currentTabWidth / lineOffset).toInt()), 0
                            )
                        }
                        .width(60.dp) // Ширина полоски
                        .height(3.dp)
                        .background(Color.White, RoundedCornerShape(1.dp)),
                )
            }
        }
        Spacer(modifier = Modifier.height(5.dp))

        HorizontalPager(
            count = tabTitles.size,
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            tabContent(page)
        }

    }
}