rootProject.name = "graph-notes"
include("backend:notes-service")
include("clients:android")
include("clients:web")
findProject(":backend:notes-service")?.name = "notes-service"
findProject(":clients:android")?.name = "android"
findProject(":clients:web")?.name = "web"

pluginManagement{

    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }

    plugins{
        fun String.getVersion() = extra["$this.version"].toString()
        // Kotlin
        kotlin("jvm") version "kotlin".getVersion()
        kotlin("plugin.allopen") version "kotlin".getVersion()

        // Web
        kotlin("multiplatform").version(extra["kotlin.version"] as String)
        id("org.jetbrains.compose").version(extra["compose.version"] as String)

        // Quarkus
        id("io.quarkus") version "quarkus".getVersion()

        // Android
        id("org.jetbrains.kotlin.android") version "kotlin".getVersion()
        id("com.android.application") version "7.2.2"
    }
}

dependencyResolutionManagement{
    repositories {
        google()
        mavenCentral()
        mavenLocal()
    }
}