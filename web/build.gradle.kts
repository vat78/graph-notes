plugins {
    kotlin("js")
    kotlin("plugin.serialization")
}

version = "1.0-SNAPSHOT"

val kotlinReactVersion: String = "17.0.2-pre.322-kotlin-1.6.10"

kotlin {
    js {
        browser {
            commonWebpackConfig {
                cssSupport.enabled = true
            }
        }
        binaries.executable()
    }
}

dependencies {
    implementation("org.jetbrains.kotlin-wrappers:kotlin-react:$kotlinReactVersion")
    implementation("org.jetbrains.kotlin-wrappers:kotlin-react-dom:$kotlinReactVersion")
    implementation(npm("react", "17.0.2"))
    implementation(npm("react-dom", "17.0.2"))

    //Kotlin React CSS (chapter 3)
    implementation("org.jetbrains.kotlin-wrappers:kotlin-react-css:$kotlinReactVersion")
}
