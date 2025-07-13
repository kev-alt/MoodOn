pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
    plugins {
        id("com.android.application") version "8.1.1"
        id("org.jetbrains.kotlin.android") version "1.9.23"
        id("org.jetbrains.kotlin.plugin.parcelize") version "1.9.23"
        id("com.google.dagger.hilt.android") version "2.51.1"
        id("com.google.gms.google-services") version "4.4.2"
        id("com.google.devtools.ksp") version "1.9.23-1.0.20"

    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "MoodOn"
include(":app")
