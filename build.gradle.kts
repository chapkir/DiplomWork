// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false

    alias(libs.plugins.ksp.plugin) apply false
    alias(libs.plugins.hilt.plugin) apply false

    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.plugin.serialization) apply false

    id("com.mikepenz.aboutlibraries.plugin") version "12.1.2" apply false
    id("com.google.gms.google-services") version "4.4.2" apply false
}
