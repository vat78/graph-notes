plugins {
    alias(libs.plugins.android.app.plugin)
    alias(libs.plugins.kotlin.android.plugin)
    alias(libs.plugins.google.services.plugin)
}

android {
    compileSdk = 33
    namespace = "ru.vat78.notes.clients.android"

    defaultConfig {
        applicationId = "ru.vat78.notes.clients.android"
        minSdk = 30
        targetSdk = 32
        versionCode = 1
        versionName = "0.1.4"

        testInstrumentationRunner = "android.support.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.5"
    }

}

dependencies {

    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material)
    implementation(libs.androidx.compose.material.iconsExtended)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.navigation.fragment)
    implementation(libs.androidx.lifecycle.viewModelCompose)
    implementation(libs.androidx.compose.ui.googlefonts)
    implementation(libs.androidx.compose.runtime.livedata)

    implementation(libs.google.accompanist.permissions)
    implementation(libs.google.play.services.auth)

    implementation(platform(libs.firebase.bom))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation(libs.firebase.ui.auth)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.espresso.core)

    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
}