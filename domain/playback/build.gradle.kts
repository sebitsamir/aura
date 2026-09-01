plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.aura.domain.playback"
    compileSdk = 36
    defaultConfig { minSdk = 26 }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:playback"))
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.javax.inject)
}
