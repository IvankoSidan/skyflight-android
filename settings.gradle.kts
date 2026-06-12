@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        maven("https://maven.aliyun.com/repository/public")
        maven("https://maven.aliyun.com/repository/google")
        maven("https://maven.aliyun.com/repository/gradle-plugin")
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
}

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

rootProject.name = "SkyFlightBooking"

include(":app")
include(":core:model")
include(":core:common")
include(":core:network")
include(":core:datastore")
include(":core:ui")
include(":core:database")
include(":feature:auth")
include(":feature:search")
include(":feature:booking")
include(":feature:notifications")
include(":navigation")
include(":feature:referral")
include(":feature:review")
include(":feature:loyalty")
include(":feature:cards")
include(":feature:invoice")