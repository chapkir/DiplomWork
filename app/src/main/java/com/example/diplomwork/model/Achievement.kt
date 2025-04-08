package com.example.diplomwork.model

data class Achievement(
    val id: Int,
    val name: String,
    val description: String,
    val icon: Int,
    val condition: AchievementCondition,
    var isUnlocked: Boolean = false
)

enum class AchievementCondition {
    FIRST_POST,
    TEN_LIKES,
}
