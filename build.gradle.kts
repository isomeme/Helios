// Root build config.

plugins {
  alias(libs.plugins.androidApplication) apply false
  alias(libs.plugins.hiltAndroid) apply false
  alias(libs.plugins.ksp) apply false
  alias(libs.plugins.googleServices) apply false
  alias(libs.plugins.kotlinCompose) apply false

  // Intentionally allowing this to operate at root level.
  alias(libs.plugins.benManesVersions)
}

tasks.register<Delete>("clean") { delete(rootProject.layout.buildDirectory) }

tasks.withType<JavaCompile> {
  options.compilerArgs.addAll(listOf("-Xlint:unchecked", "-Xlint:deprecation"))
}
