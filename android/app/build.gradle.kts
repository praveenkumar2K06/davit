import java.util.Properties
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

// Release signing comes from android/keystore.properties (gitignored): storeFile, passwordFile
// and keyAlias. Without it, release builds are unsigned.
val keystore = Properties().apply {
    rootProject.file("keystore.properties").takeIf { it.exists() }?.inputStream()?.use { load(it) }
}

android {
    namespace = "dev.medriid.davit"
    compileSdk = 37

    defaultConfig {
        applicationId = "dev.medriid.davit"
        minSdk = 26
        targetSdk = 37
        versionCode = 32
        versionName = "1.0.1"
    }

    signingConfigs {
        if (keystore.isNotEmpty()) {
            create("release") {
                val password = file(keystore.getProperty("passwordFile")).readText().trim()
                storeFile = file(keystore.getProperty("storeFile"))
                storePassword = password
                keyAlias = keystore.getProperty("keyAlias")
                keyPassword = password
            }
        }
    }

    buildTypes {
        // Dev builds install next to the release one instead of clashing with its signature.
        debug {
            applicationIdSuffix = ".dev"
            resValue("string", "app_name", "daVIT dev")
        }
        release {
            resValue("string", "app_name", "daVIT")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.findByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        resValues = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.fromTarget("17")
    }
}

dependencies {
    implementation(project(":design"))
    implementation(project(":core"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.work)
    implementation(libs.glance.appwidget)
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.zxing.core)
    val bom = platform(libs.compose.bom)
    implementation(bom)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    debugImplementation(libs.compose.ui.tooling)
    // The demo's fake VTOP (src/debug): writes are tried against it, never the real one.
    debugImplementation(libs.okhttp.mockwebserver)
    testImplementation(libs.junit)
    testImplementation("org.json:json:20260814")
}
