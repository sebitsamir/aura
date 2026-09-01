plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.aura.feature.playlists"
    compileSdk = 36
    defaultConfig { minSdk = 26 }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

