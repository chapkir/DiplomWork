package com.example.diplomwork.ui.main_screen.top_bar

import com.example.diplomwork.R
import com.example.diplomwork.ui.main_screen.bottom_menu.BottomMenuItem

sealed class TopBarItem(
    val route: String,
    val title: String,
    val iconId: Int
) {
    object Search: TopBarItem(
        route = "search",
        title =  "",
        iconId = R.drawable.ic_search
    )
    object FilterList: TopBarItem(
        route = "search",
        title =  "",
        iconId = R.drawable.ic_filter_list
    )
}