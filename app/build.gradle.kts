plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "student.inti.signuplogin"
    compileSdk = 34

    defaultConfig {
        applicationId = "student.inti.signuplogin"
        minSdk = 34
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        vectorDrawables.useSupportLibrary = true

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildFeatures{
            viewBinding =true
        }
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

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.storage)
    implementation(libs.firebase.database)
    implementation(libs.swiperefreshlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation ("com.google.firebase:firebase-auth")
    implementation ("com.google.firebase:firebase-database:20.1.0")
    implementation ("com.google.firebase:firebase-storage:20.1.0")
    //firestore
    implementation ("com.google.firebase:firebase-firestore:24.7.1")

    //Recycler View Swipe Decorator
    implementation ("it.xabaras.android:recyclerview-swipedecorator:1.4")

    implementation(platform("com.google.firebase:firebase-bom:33.3.0"))
    implementation ("androidx.constraintlayout:constraintlayout:2.0.0-alpha4")
    implementation("androidx.core:core:1.13.0")
    implementation ("androidx.work:work-runtime:2.8.1")

    // Add the Guava library dependency
    implementation ("com.google.guava:guava:31.1-android")
    //recycleView
    implementation ("com.github.bumptech.glide:glide:4.12.0")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.12.0") // for annotation processing

}