plugins {
  alias(libs.plugins.androidApplication)
  alias(libs.plugins.googleServices)
  alias(libs.plugins.hiltAndroid)
  alias(libs.plugins.kotlinAndroid)
  alias(libs.plugins.kotlinCompose)
  alias(libs.plugins.kotlinSerialization)
  alias(libs.plugins.ksp)
}

android {
  namespace = "org.onereed.helios"
  compileSdk = 36

  defaultConfig {
    applicationId = "org.onereed.helios"
    versionCode = 21
    versionName = "3.0.1"

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

      ndk {
        debugSymbolLevel = "FULL"
      }
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

  kotlin {
    jvmToolchain(17)

    compilerOptions {
      freeCompilerArgs.add("-Xannotation-default-target=param-property")
    }
  }

  buildFeatures {
    buildConfig = true
    viewBinding = true
    compose = true
  }

  testOptions { unitTests.isReturnDefaultValues = true }
}

dependencies {

  // Required for Java 8+ APIs on API levels < 33
  coreLibraryDesugaring(libs.desugarJdkLibsNio)

  // Core Dependencies
  implementation(libs.coreKtx)
  implementation(libs.bundles.composeRuntime)
  implementation(libs.bundles.navigationRuntime)
  implementation(libs.bundles.hiltRuntime)
  ksp(libs.bundles.hiltProcessor)

  // Compose BOM
  val composeBom = platform(libs.composeBom)
  implementation(composeBom)
  debugImplementation(composeBom)
  testImplementation(composeBom)
  androidTestImplementation(composeBom)

  // Services
  implementation(libs.firebaseAnalytics)
  implementation(libs.playServicesLocation)

  // Android
  implementation(libs.appcompat)
  implementation(libs.cardview)
  implementation(libs.constraintlayout)
  implementation(libs.recyclerview)
  implementation(libs.material)

  // Misc androidx
  implementation(libs.activityKtx)
  implementation(libs.datastorePreferences)

  // Lifecycle
  implementation(libs.bundles.lifecycleRuntime)

  // Misc Kotlin libraries
  implementation(libs.collectionKtx)
  implementation(libs.kotlinxDatetime)
  implementation(libs.kotlinxSerializationJson)

  // Misc 3rd-party libraries
  implementation(libs.accompanistPermissions)
  implementation(libs.bundles.markdownRuntime)
  implementation(libs.suncalc)
  implementation(libs.timber)

  // Debug and Tooling Dependencies
  debugImplementation(libs.bundles.composeDebug)

  // Local Unit Tests
  testImplementation(libs.bundles.unitTest)

  // Instrumented Android Tests
  androidTestImplementation(libs.bundles.androidTest)
}
