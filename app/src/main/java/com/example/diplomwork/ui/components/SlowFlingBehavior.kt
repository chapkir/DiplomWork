package com.example.diplomwork.ui.components

import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.ScrollScope
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
fun rememberSlowFlingBehavior(): FlingBehavior {
    val defaultFling = ScrollableDefaults.flingBehavior()

    return remember {
        object : FlingBehavior {
            override suspend fun ScrollScope.performFling(initialVelocity: Float): Float {
                val slowedVelocity = initialVelocity / 1.3f
                return with(defaultFling) {
                    performFling(slowedVelocity)
                }
            }
        }
    }
}