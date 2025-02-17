package com.example.diplomwork.frontend.ui.screens.main_screen.top_bar

import com.example.diplomwork.R

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