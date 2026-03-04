import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
    id("org.jetbrains.kotlin.native.cocoapods")
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    
    iosArm64()
    iosSimulatorArm64()

    cocoapods {
        summary = "Shared module for MediaCCC"
        homepage = "https://github.com/mediaccc"
        version = "1.0"
        ios.deploymentTarget = "16.0"
        
        framework {
            baseName = "shared"
            isStatic = true
        }

        // The Pod name is 'google-cast-sdk' but the module name is 'GoogleCast'
        pod("google-cast-sdk") {
            moduleName = "GoogleCast"
            version = "4.8.3"
        }
    }
    
    sourceSets {
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.koin.android)
            api(libs.androidx.lifecycle.viewmodel.navigation3)
            implementation(libs.slf4j.simple)
            implementation(libs.play.services.cast.framework)
        }
        commonMain.dependencies {
            implementation(project(":api"))
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.composemediaplayer)
            implementation(libs.ktor.client.mock)
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor)
            implementation(libs.coil.svg)
            api(libs.navigation3.ui)
            api(libs.koin.core)
            api(libs.koin.compose)
            api(libs.koin.compose.viewmodel)
            api(libs.koin.compose.viewmodel.navigation)
            implementation(libs.room.runtime)
        }
        iosMain.dependencies {
            implementation(libs.sqlite.bundled)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.koin.test)
            implementation(libs.ktor.client.mock)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}

android {
    namespace = "com.jaeckel.mediaccc"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    debugImplementation(libs.compose.uiTooling)
    add("kspAndroid", libs.room.compiler)
    add("kspIosArm64", libs.room.compiler)
    add("kspIosSimulatorArm64", libs.room.compiler)
}

room {
    schemaDirectory("$projectDir/schemas")
}

compose.resources {
    publicResClass = true
    packageOfResClass = "mediaccc.shared.generated.resources"
}
