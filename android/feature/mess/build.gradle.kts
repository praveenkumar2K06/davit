import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "dev.medriid.davit.feature.mess"
    compileSdk = 37

    defaultConfig {
        minSdk = 26
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.fromTarget("17")
    }
}

dependencies {
    api(project(":core"))
    api(project(":core:model"))
    api(project(":core:data"))
    implementation(project(":design"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    val bom = platform(libs.compose.bom)
    implementation(bom)
    implementation(libs.compose.material3)

    testImplementation(libs.junit)
}
