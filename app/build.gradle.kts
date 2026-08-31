plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.lockscreen"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.lockscreen"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
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
        // Turns on Jetpack Compose for this module.
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Lets a Composable collect a StateFlow in a lifecycle-aware way.
    implementation(libs.androidx.lifecycle.runtime.compose)
    // Provides viewModel() so a Composable can obtain a ViewModel.
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)

    // The BOM aligns all Compose artifact versions with each other.
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    // Tooling is only needed for the Android Studio preview / layout inspector.
    debugImplementation(libs.androidx.compose.ui.tooling)

    // Plain JVM unit tests for the attempt logic – no device required.
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
