plugins {
  id("com.android.application")
  id("com.google.dagger.hilt.android")
  id("com.google.gms.google-services")
  id("org.jetbrains.kotlin.android")
  id("org.jetbrains.kotlin.plugin.compose")

  // This may need to be the last ordinary plugin, to pick up resources from other plugins.
  id("com.google.devtools.ksp")

  // Kotlin serialization plugin for type safe routes and navigation arguments.
  kotlin("plugin.serialization") version "2.0.21"
}

android {
  namespace = "org.onereed.helios"
  compileSdk = 36

  defaultConfig {
    applicationId = "org.onereed.helios"
    versionCode = 19
    versionName = "2.3.2"

    minSdk = 26
    targetSdk = 36

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  buildTypes {
    getByName("release") {
      isMinifyEnabled = true
      isShrinkResources = true
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
      versionNameSuffix = ""
    }
    getByName("debug") { versionNameSuffix = " (debug)" }
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
    isCoreLibraryDesugaringEnabled = true
  }

  kotlin { jvmToolchain(17) }

  buildFeatures {
    buildConfig = true
    viewBinding = true
    compose = true
  }

  testOptions { unitTests.isReturnDefaultValues = true }
}

dependencies {

  // Language

  coreLibraryDesugaring("com.android.tools:desugar_jdk_libs_nio:2.1.5")

  // Services

  implementation("com.google.firebase:firebase-analytics:23.0.0")
  implementation("com.google.android.gms:play-services-location:21.3.0")

  // Android

  implementation("androidx.appcompat:appcompat:1.7.1")
  implementation("androidx.cardview:cardview:1.0.0")
  implementation("androidx.constraintlayout:constraintlayout:2.2.1")
  implementation("androidx.recyclerview:recyclerview:1.4.0")
  implementation("com.google.android.material:material:1.13.0")

  // Android Kotlin

  // Core KTX
  implementation("androidx.core:core-ktx:1.17.0")

  // Activity & Fragment KTX
  implementation("androidx.activity:activity-ktx:1.11.0")
  implementation("androidx.fragment:fragment-ktx:1.8.9")

  // Lifecycle & ViewModel KTX (use a single version variable)
  val lifecycleVersion = "2.9.4"
  implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycleVersion")
  implementation("androidx.lifecycle:lifecycle-livedata-ktx:$lifecycleVersion")
  implementation("androidx.lifecycle:lifecycle-runtime-ktx:$lifecycleVersion")
  implementation("androidx.lifecycle:lifecycle-viewmodel-compose:$lifecycleVersion")

  // Navigation KTX (use a single version variable)
  val navVersion = "2.9.5"
  implementation("androidx.navigation:navigation-compose:${navVersion}")
  implementation("androidx.navigation:navigation-dynamic-features-fragment:${navVersion}")
  implementation("androidx.navigation:navigation-fragment-ktx:$navVersion")
  implementation("androidx.navigation:navigation-ui-ktx:$navVersion")
  androidTestImplementation("androidx.navigation:navigation-testing:$navVersion")

  // JSON serialization library, works with the Kotlin serialization plugin
  implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")

  // Collection KTX
  implementation("androidx.collection:collection-ktx:1.5.0")

  // Compose

  implementation("androidx.activity:activity-compose:1.11.0")
  implementation("androidx.constraintlayout:constraintlayout-compose:1.1.1")
  implementation("androidx.work:work-runtime:2.11.0")

  val composeBom = platform("androidx.compose:compose-bom:2025.10.01")
  implementation(composeBom)
  testImplementation(composeBom)
  androidTestImplementation(composeBom)

  implementation("androidx.compose.material3:material3")
  implementation("androidx.compose.material3:material3-adaptive-navigation-suite")
  implementation("androidx.compose.ui:ui")
  implementation("androidx.compose.ui:ui-graphics")
  implementation("androidx.compose.ui:ui-text-google-fonts")
  implementation("androidx.compose.ui:ui-tooling-preview")

  debugImplementation("androidx.compose.ui:ui-test-manifest")
  debugImplementation("androidx.compose.ui:ui-tooling")

  // Hilt

  val hiltVersion = "2.57.2"
  implementation("com.google.dagger:hilt-android:$hiltVersion")
  ksp("com.google.dagger:hilt-compiler:$hiltVersion")

  val androidxHiltVersion = "1.3.0"
  implementation("androidx.hilt:hilt-navigation-compose:${androidxHiltVersion}")
  implementation("androidx.hilt:hilt-navigation-fragment:${androidxHiltVersion}")
  implementation("androidx.hilt:hilt-work:$androidxHiltVersion")
  ksp("androidx.hilt:hilt-compiler:$androidxHiltVersion")

  // SunCalc

  implementation("org.shredzone.commons:commons-suncalc:3.11")

  // Guava

  implementation("com.google.guava:guava:33.5.0-android")
  implementation("com.google.auto.value:auto-value-annotations:1.11.0")

  // Markdown support

  implementation("com.github.jeziellago:compose-markdown:0.5.7")

  // Logging

  implementation("com.jakewharton.timber:timber:5.0.1")

  // Testing

  testImplementation("com.google.truth:truth:1.4.5")
  testImplementation("junit:junit:4.13.2")
  testImplementation("org.jetbrains.kotlin:kotlin-test:2.2.21")
  testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
  testImplementation("org.junit.jupiter:junit-jupiter:6.0.1")
  testImplementation("org.mockito:mockito-core:5.20.0")

  androidTestImplementation("androidx.compose.ui:ui-test-junit4")
  androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
  androidTestImplementation("androidx.test.ext:junit:1.3.0")
}
