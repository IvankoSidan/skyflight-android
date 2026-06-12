pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
}

rootProject.name = "SkyFlightBooking"

include(
    ":app",
    ":core:model",
    ":core:common",
    ":core:network",
    ":core:datastore",
    ":core:ui",
    ":core:database",
    ":core:config",
    ":feature:auth",
    ":feature:search",
    ":feature:booking",
    ":feature:notifications",
    ":navigation",
    ":feature:referral",
    ":feature:review",
    ":feature:loyalty",
    ":feature:cards",
    ":feature:invoice"
)