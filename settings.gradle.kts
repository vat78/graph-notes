rootProject.name = "graph-notes"
include("backend:notes-service")
include("web")
findProject(":backend:notes-service")?.name = "notes-service"

pluginManagement{
    plugins{
        fun String.getVersion() = extra["$this.version"].toString()
        // Kotlin
        kotlin("jvm") version "kotlin".getVersion()
        kotlin("js") version "kotlin".getVersion()
        kotlin("plugin.allopen") version "kotlin".getVersion()
        kotlin("plugin.serialization") version "kotlin".getVersion()

        // Quarkus
        id("io.quarkus") version "quarkus".getVersion()
    }
}

dependencyResolutionManagement{
    repositories {
        mavenCentral()
        mavenLocal()
    }
}