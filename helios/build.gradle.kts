plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.hilt.android)
  alias(libs.plugins.google.services)
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.ksp)
  alias(libs.plugins.kotlin.serialization)
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
    val commonProguardFiles =
      listOf(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")

    getByName("release") {
      isMinifyEnabled = true
      isShrinkResources = true
      setProguardFiles(commonProguardFiles)
      versionNameSuffix = ""
    }

    getByName("debug") { versionNameSuffix = " (debug)" }

    create("staging") {
      initWith(getByName("debug"))
      isDebuggable = false
      isMinifyEnabled = true
      isShrinkResources = true
      setProguardFiles(commonProguardFiles)
      versionNameSuffix = " (staging)"
    }
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
    isCoreLibraryDesugaringEnabled = true
  }

  // See https://stackoverflow.com/questions/65124097
  lint.disable += "UseCompatLoadingForDrawables"

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

  coreLibraryDesugaring(libs.desugar.jdk.libs.nio)

  // Bumdles

  implementation(libs.bundles.compose)
  implementation(libs.bundles.navigation)

  // Services

  implementation(libs.firebase.analytics)
  implementation(libs.play.services.location)

  // Android

  implementation(libs.appcompat)
  implementation(libs.cardview)
  implementation(libs.constraintlayout)
  implementation(libs.recyclerview)
  implementation(libs.material)

  // Android Kotlin

  // Core KTX
  implementation(libs.core.ktx)

  // Misc androidx
  implementation(libs.activity.ktx)
  implementation(libs.datastore.preferences)
  implementation(libs.fragment.ktx)

  // Lifecycle & ViewModel KTX
  implementation(libs.lifecycle.viewmodel.ktx)
  implementation(libs.lifecycle.livedata.ktx)
  implementation(libs.lifecycle.runtime.ktx)
  implementation(libs.lifecycle.viewmodel.compose)

  // Navigation KTX
  implementation(libs.navigation.compose)
  implementation(libs.navigation.dynamic.features.fragment)
  androidTestImplementation(libs.navigation.testing)

  // JSON serialization library, works with the Kotlin serialization plugin
  implementation(libs.kotlinx.serialization.json)

  // Kotlin date-time library
  implementation(libs.kotlinx.datetime)

  // Collection KTX
  implementation(libs.collection.ktx)

  // Compose

  implementation(libs.work.runtime)

  val composeBom = platform(libs.compose.bom)
  implementation(composeBom)
  debugImplementation(composeBom)
  testImplementation(composeBom)
  androidTestImplementation(composeBom)

  implementation(libs.material3.adaptive.navigation.suite)
  implementation(libs.ui.graphics)

  debugImplementation(libs.ui.tooling)

  // Not in bom

  implementation(libs.ui.text.google.fonts)
  debugImplementation(libs.ui.test.manifest)

  // Hilt

  implementation(libs.hilt.android)
  ksp(libs.hilt.compiler)

  implementation(libs.androidx.hilt.navigation.compose)
  implementation(libs.androidx.hilt.navigation.fragment)
  implementation(libs.androidx.hilt.work)
  ksp(libs.androidx.hilt.compiler)

  // SunCalc

  implementation(libs.suncalc)

  // Markdown support

  implementation(libs.richtext.ui.material3)
  implementation(libs.richtext.commonmark)

  // Accompanist (permission management)

  implementation(libs.accompanist.permissions)

  // Logging

  implementation(libs.timber)

  // Testing

  testImplementation(libs.truth)
  testImplementation(libs.junit)
  testImplementation(libs.kotlin.test)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.junit.jupiter)
  testImplementation(libs.mockito.core)

  androidTestImplementation(libs.ui.test.junit4)
  androidTestImplementation(libs.espresso.core)
  androidTestImplementation(libs.androidx.test.ext.junit)
}
