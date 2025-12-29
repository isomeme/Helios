// Root build config.

plugins {
  alias(libs.plugins.android.application) apply false
  alias(libs.plugins.hilt.android) apply false
  alias(libs.plugins.ksp) apply false
  alias(libs.plugins.google.services) apply false
  alias(libs.plugins.kotlin.android) apply false
  alias(libs.plugins.kotlin.compose) apply false

  // Intentionally allowing this to operate at root level.
  alias(libs.plugins.ben.manes.versions)
}

tasks.register<Delete>("clean") { delete(rootProject.layout.buildDirectory) }

tasks.withType<JavaCompile> {
  options.compilerArgs.addAll(listOf("-Xlint:unchecked", "-Xlint:deprecation"))
}
