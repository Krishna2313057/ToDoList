pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        gradlePluginPortal()
        mavenCentral()
        maven("https://jitpack.io") // ✅ Add JitPack for plugin management (safe)
    }
    plugins {
        id("androidx.navigation.safeargs.kotlin") version "2.7.7"
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()

        // ✅ ADD THIS:
        maven(url = "https://jitpack.io")
    }
}


rootProject.name = "ToDoList"
include(":app")
