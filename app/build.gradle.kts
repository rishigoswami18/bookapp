

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.google.firebase.crashlytics)
}

android {
    namespace = "com.hrishikeshbooks.bookapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.hrishikeshbooks.bookapp"
        minSdk = 23
        targetSdk = 35
        versionCode = 2
        versionName = "2.O"

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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures{
        viewBinding = true
    }
}

dependencies {

    implementation(libs.android.pdf.viewer)

    implementation(libs.coil.compose)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.firebase.analytics)
    //implementation(libs.firebase.database)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.storage)
    implementation(libs.firebase.database.ktx)
    //implementation(libs.firebase.database.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    //implementation (platform(libs.firebase.bom) )
    implementation("androidx.compose.ui:ui:1.6.7") // Check version!
    implementation("androidx.compose.material3:material3:1.2.1")
    implementation("com.google.android.gms:play-services-ads:24.1.0")

    implementation("com.github.bumptech.glide:glide:4.15.1")
    annotationProcessor("com.github.bumptech.glide:compiler:4.15.1")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation ("com.google.firebase:firebase-appcheck-debug:17.0.0")
    implementation ("com.squareup.okhttp3:okhttp:4.12.0")
    implementation ("com.google.android.gms:play-services-ads:22.5.0") // or latest stable version

    implementation ("com.google.android.gms:play-services-ads:22.6.0")

    //implementation( "com.google.android.gms:play-services-ads:24.4.0")

    // If using mediation, update those too
    implementation ("com.google.android.ads:mediation-test-suite:3.0.0")
    implementation ("com.android.billingclient:billing:6.1.0")
    implementation ("com.razorpay:checkout:1.6.33")





















}