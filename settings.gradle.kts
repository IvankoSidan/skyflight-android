import org.gradle.api.initialization.resolve.RepositoriesMode

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }

    resolutionStrategy {
        eachPlugin {
            when (requested.id.id) {
                "com.google.gms.google-services" -> {
                    useModule("com.google.gms:google-services:${requested.version}")
                }
                "com.android.application",
                "com.android.library" -> {
                    useModule("com.android.tools.build:gradle:${requested.version}")
                }
                "org.jetbrains.kotlin.android" -> {
                    useModule("org.jetbrains.kotlin:kotlin-gradle-plugin:${requested.version}")
                }
            }
        }
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
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
    ":feature:referral",
    ":feature:review",
    ":feature:loyalty",
    ":feature:cards",
    ":feature:invoice",
    ":navigation"
)