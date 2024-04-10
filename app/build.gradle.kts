import java.text.SimpleDateFormat
import java.util.Date

plugins {
    id("com.android.application")

    id("io.sentry.android.gradle") version "4.2.0"


    id("com.google.gms.google-services") version "4.4.1" apply false
//    id("com.google.gms.google-services")
}

android {
    namespace = "it.letscode.simappdevice"
    compileSdk = 34

    defaultConfig {
        applicationId = "it.letscode.simappdevice"
        minSdk = 23
        targetSdk = 34

        versionCode = generateVersionCode()
        versionName = generateVersionName()

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
    implementation("com.squareup.okhttp3:okhttp:4.9.0");
    implementation("com.google.android.gms:play-services-location:18.0.0");

    implementation(platform("com.google.firebase:firebase-bom:32.8.0"))
    implementation("com.google.firebase:firebase-analytics")
}

sentry {
    org.set("lets-code-it-y3")
    projectName.set("sim-app-device")

    // this will upload your source code to Sentry to show it as part of the stack traces
    // disable if you don't want to expose your sources
    includeSourceContext.set(true)
}

fun generateVersionCode(): Int {
    return (Date().time / 1000).toInt()
}

fun generateVersionName(): String {
    val dateFormat = SimpleDateFormat("yyyy.MM.dd.HH.mm.ss")
    return dateFormat.format(Date())
}
