rootProject.name = "graph-notes"
include("backend:notes-service")
include("clients:android")
include("clients:web")

pluginManagement{

    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}

dependencyResolutionManagement{
    repositories {
        google()
        mavenCentral()
        mavenLocal()
    }
}