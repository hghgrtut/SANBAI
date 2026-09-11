plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.ksp)
}

android {
    namespace = "by.rowing.sanbaiteam.data.local"
    compileSdk = 37

    defaultConfig {
        minSdk = 25
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    ksp(libs.room.compiler)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    implementation(libs.coroutines.android)
    implementation(libs.koin.core)
    implementation(libs.koin.android)
}