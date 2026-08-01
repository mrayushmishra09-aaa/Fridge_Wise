plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.fridgewise"
    // API 35 is the stable Android 15 SDK
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.fridgewise"
        minSdk = 24
        targetSdk = 35
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
        // Java 17 is required for recent Android Gradle Plugin versions
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.legacy.support.v4)
    implementation(libs.cardview)
    implementation(libs.fragment)
    implementation(libs.recyclerview)

    // Room Database
    implementation(libs.roomruntime)
    annotationProcessor(libs.roomcompiler)
    implementation(libs.roomcommon)

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
