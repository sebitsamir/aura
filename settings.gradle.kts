pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Aura"
include(":app")

// When you need feature/library modules, add them here, e.g.:
// Core Modules
include(":core:common")
include(":core:database")
include(":core:datastore")
include(":core:designsystem")
include(":core:model")
include(":core:media")
include(":core:network")
include(":core:permissions")
include(":core:playback")
include(":core:scanner")
include(":core:analytics")
include(":core:utilities")

// Data Modules
include(":data:repository")

// Domain Modules
include(":domain:playback")

// Feature Modules
include(":feature:home")
include(":feature:library")
include(":feature:songs")
include(":feature:albums")
include(":feature:artists")
include(":feature:playlists")
include(":feature:search")
include(":feature:player")
include(":feature:queue")
include(":feature:lyrics")
include(":feature:flow")
include(":feature:statistics")
include(":feature:settings")

// Service Modules
include(":service:playback")