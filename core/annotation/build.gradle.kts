plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "by.rowing.sanbaiteam.core.annotation"
    compileSdk = 37

    defaultConfig {
        minSdk = 25
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}