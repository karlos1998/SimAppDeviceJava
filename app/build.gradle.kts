plugins {
    id("com.android.application")
}

android {
    namespace = "it.letscode.simappdevice"
    compileSdk = 34

    defaultConfig {
        applicationId = "it.letscode.simappdevice"
        minSdk = 23
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8

        isCoreLibraryDesugaringEnabled = true //need to pusher for android 5 / 6
    }

}

dependencies {

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.8.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    implementation("org.nanohttpd:nanohttpd:2.3.1")
    implementation("com.github.spullara.mustache.java:compiler:0.8.18")
    implementation("com.google.guava:listenablefuture:9999.0-empty-to-avoid-conflict-with-guava")
    implementation("com.pusher:pusher-java-client:2.4.4")
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.3") //need to pusher for android 5 / 6
}