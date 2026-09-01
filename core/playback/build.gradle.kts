plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.aura.core.playback"
    compileSdk = 36
    defaultConfig { minSdk = 26 }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(project(":core:model"))
    implementation(libs.media3.common)
    implementation(libs.androidx.core.ktx)

    testImplementation(libs.junit)
}
