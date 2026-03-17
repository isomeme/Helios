pluginManagement {
  repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
  }
}

plugins { id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0" }

dependencyResolutionManagement {
  repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
  repositories {
    google()
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
  }

  versionCatalogs {
    create("sharedLibs") {
      from(files("submodules/Shared/gradle/libs.versions.toml"))
    }
  }
}

rootProject.name = "Helios"
include(":helios")

include(":Shared")
project(":Shared").projectDir = file("submodules/Shared")

include(":Shared:app")
project(":Shared:app").projectDir = file("submodules/Shared/app")
