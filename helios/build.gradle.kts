import org.gradle.api.JavaVersion.VERSION_11

plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "org.onereed.helios"
    compileSdk = 36

    defaultConfig {
        applicationId = "org.onereed.helios"
        minSdk = 26
        targetSdk = 35
        versionCode = 19
        versionName = "2.3.2"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            versionNameSuffix = ""
        }
        getByName("debug") {
            versionNameSuffix = " (debug)"
        }
    }

    compileOptions {
        sourceCompatibility = VERSION_11
        targetCompatibility = VERSION_11
        isCoreLibraryDesugaringEnabled = true
    }

    kotlin {
        jvmToolchain(11)
    }

    buildFeatures {
        buildConfig = true
        viewBinding = true
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

dependencies {

    // Helios

    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    // Language

    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.5")

    // Services

    implementation("com.google.firebase:firebase-analytics:23.0.0")
    implementation("com.google.android.gms:play-services-location:21.3.0")

    // Android

    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    implementation("androidx.core:core-ktx:1.17.0")
    implementation("androidx.lifecycle:lifecycle-common-java8:2.9.4")
    implementation("androidx.recyclerview:recyclerview:1.4.0")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")

    // SunCalc

    implementation("org.shredzone.commons:commons-suncalc:3.11")

    // Guava

    implementation("com.google.guava:guava:33.5.0-android")
    implementation("com.google.auto.value:auto-value-annotations:1.11.0")
    annotationProcessor("com.google.auto.value:auto-value:1.11.0")

    // Logging

    implementation("com.jakewharton.timber:timber:5.0.1")

    // Testing

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito:mockito-core:5.20.0")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
}

tasks.withType<JavaCompile> {
    options.compilerArgs.addAll(listOf("-Xlint:unchecked", "-Xlint:deprecation"))
}
