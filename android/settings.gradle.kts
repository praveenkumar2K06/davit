pluginManagement {
    repositories {
        google()
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

rootProject.name = "daVIT"
include(
    ":app",
    ":design",
    ":core",
    ":core:model",
    ":core:data",
    ":feature:scanner",
    ":feature:map",
    ":feature:academics",
    ":feature:ffcs",
    ":feature:materials",
    ":feature:mess",
    ":feature:settings",
)
