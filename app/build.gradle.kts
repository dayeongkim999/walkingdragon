plugins {
    alias(libs.plugins.androidApplication)
}

android {
    namespace = "com.example.walkingdragon"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.walkingdragon"
        minSdk = 28
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    val work_version = "2.9.1"
    // (Java only)
    implementation("androidx.work:work-runtime:$work_version")
    // Retrofit 라이브러리
    implementation("com.google.maps:google-maps-services:0.18.0")
    implementation("org.slf4j:slf4j-simple:1.7.25")
    implementation("org.locationtech.proj4j:proj4j:1.2.2")
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}