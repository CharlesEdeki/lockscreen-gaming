pluginManagement {
    repositories { google(); mavenCentral(); gradlePluginPortal() }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories { google(); mavenCentral() }
}
rootProject.name = "lockscreen-gaming"
include(":app", ":core:engine", ":feature:lockscreen", ":games:snake", ":games:bricks", ":games:dodge")
