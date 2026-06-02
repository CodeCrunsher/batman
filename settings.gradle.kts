pluginManagement {
    repositories {
        maven {
            // maven.google.com via direct IP (DNS blocked in this environment)
            url = uri("https://maven.google.com/")
            content {
                includeGroupByRegex("androidx.*")
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        // Local pre-downloaded artifacts fallback (for when maven.google.com DNS is intermittent)
        maven {
            url = uri("file:///C:/Users/Lenovo/.gradle/batman-local-repo")
            content {
                includeGroupByRegex("androidx.*")
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
            }
        }
        maven {
            url = uri("https://maven.google.com/")
            content {
                includeGroupByRegex("androidx.*")
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
            }
        }
        mavenCentral()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "Batman Dashboard"
include(":app")

