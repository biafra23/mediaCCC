import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    alias(libs.plugins.kotlinSerialization)
}

val keystoreProperties = Properties().apply {
    load(rootProject.file("keystore.properties").inputStream())
}

// Extract version from git tag if available, otherwise use default
val gitVersionName = providers.exec {
    commandLine("git", "describe", "--tags", "--always")
}.standardOutput.asText.get().trim().removePrefix("v")

android {
    namespace = "com.jaeckel.mediaccc.tv"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.jaeckel.mediaccc.tv"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = gitVersionName
    }

    signingConfigs {
        create("release") {
            storeFile = file(keystoreProperties["storeFile"] as String)
            storePassword = keystoreProperties["storePassword"] as String
            keyAlias = keystoreProperties["keyAlias"] as String
            keyPassword = keystoreProperties["keyPassword"] as String
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-DEBUG"
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
    }
}

dependencies {

    implementation(project(":shared"))
    implementation(project(":api"))

    implementation(libs.compose.components.resources)

    implementation(libs.androidx.leanback)
    implementation(libs.androidx.appcompat.v170)

    implementation(libs.androidx.core.ktx.v1150)
    implementation(libs.androidx.activity.compose.v193)

    // Use JetBrains Compose (from shared module) instead of AndroidX Compose BOM
    // to avoid version conflicts
    implementation(libs.compose.ui)
    implementation(libs.compose.foundation)
    implementation(libs.compose.material3)
    implementation(libs.compose.uiToolingPreview)

    // Material Icons for player controls
    implementation(libs.androidx.material.icons.extended)

    // Explicit AndroidX Compose Foundation for runtime classes like BringIntoViewResponder
    implementation(libs.androidx.foundation)

    implementation(libs.compose.multiplatform.media.player)

    // TV-specific dependencies - using stable 1.0.1 for Compose 1.10+ compatibility
    implementation(libs.androidx.tv.foundation)
    implementation(libs.androidx.tv.material)

    // Navigation 3
    implementation(libs.navigation3.ui)

    // Koin for Dependency Injection
    implementation(libs.koin.core)
    implementation(libs.koin.android)
    implementation(libs.koin.compose)
    implementation(libs.koin.compose.viewmodel)

    // Coil for image loading (Coil 3.x for KMP support)
    implementation(libs.coil.compose)
    implementation(libs.coil.network.ktor)
    implementation(libs.coil.svg)

    // Ktor client (required at runtime by compose-multiplatform-media-player)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)

    // Lifecycle
    implementation(libs.androidx.lifecycle.viewmodelCompose)
    implementation(libs.androidx.lifecycle.runtimeCompose)

    // Testing
    testImplementation(libs.kotlin.test)
    testImplementation(libs.junit)
}
