plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android { namespace = "com.qingjian.android"; compileSdk = 35
    defaultConfig { applicationId = "com.qingjian.android"; minSdk = 26; targetSdk = 35; versionCode = 1; versionName = "0.1.0" }
    sourceSets["main"].jniLibs.srcDirs("src/main/jniLibs")
}

dependencies { implementation("androidx.core:core-ktx:1.15.0") }
