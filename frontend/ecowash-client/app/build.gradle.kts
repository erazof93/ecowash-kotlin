plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.example.ecowash_client"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.ecowash_client"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
    // Jetpack Navigation Compose
    implementation("androidx.navigation:navigation-compose:2.8.5")

    // Retrofit (El "Dio" que íbamos a usar en Flutter, pero nativo)
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")

    // OkHttp Logging Interceptor (Para ver las peticiones en el Logcat)
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Jetpack DataStore (Preferencias seguras con Coroutines)
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // Coroutines de Kotlin
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    // Serializable
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.10.0")

    //splash
    implementation("androidx.core:core-splashscreen:1.0.1")

    implementation("com.squareup.retrofit2:converter-kotlinx-serialization:2.11.0")

    // Google Play Services Location
    implementation("com.google.android.gms:play-services-location:21.3.0")

    // Material Icons Core (Home, List, ShoppingCart, etc.)
    implementation("androidx.compose.material:material-icons-core")

    // Lifecycle ViewModel Compose
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")

}