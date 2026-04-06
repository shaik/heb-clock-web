import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.github.triplet.play")
}

val localProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) load(f.inputStream())
}

android {
    namespace = "com.shaik.hebclockwidget"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.shaik.hebclockwidget"
        minSdk = 26
        targetSdk = 35
        versionCode = 11
        versionName = "1.19"
    }

    signingConfigs {
        create("release") {
            storeFile     = file("${rootProject.projectDir}/${localProps["keystore.path"]}")
            storePassword = localProps["keystore.storePassword"] as String
            keyAlias      = localProps["keystore.keyAlias"] as String
            keyPassword   = localProps["keystore.keyPassword"] as String
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
            signingConfig = signingConfigs.getByName("release")
        }
        debug {
            // No suffix — same applicationId as release so new installs replace old ones
        }
    }

    applicationVariants.all {
        val variant = this
        variant.outputs.all {
            val output = this as com.android.build.gradle.internal.api.BaseVariantOutputImpl
            output.outputFileName = "hebrewClockWidget.apk"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }
}

play {
    serviceAccountCredentials = file("${rootProject.projectDir}/${localProps["play.serviceAccountJson"]}")
    track = "internal"
    defaultToAppBundles = true
}

dependencies {
    implementation("androidx.glance:glance-appwidget:1.1.0")
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
}
