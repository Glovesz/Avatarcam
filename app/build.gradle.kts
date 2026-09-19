plugins {
    id("com.android.application")
}

android {
    namespace = "com.glovesz.avatarcam"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.glovesz.avatarcam"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "0.1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
}
