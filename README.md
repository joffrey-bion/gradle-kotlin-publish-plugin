# Kotlin Publish Plugin

[![Gradle plugin version](https://img.shields.io/maven-metadata/v/https/plugins.gradle.org/m2/org/hildan/gradle/gradle-kotlin-publish-plugin/maven-metadata.xml.svg?label=gradle&logo=gradle)](https://plugins.gradle.org/plugin/org.hildan.kotlin-publish)
[![Github Build](https://img.shields.io/github/actions/workflow/status/joffrey-bion/gradle-kotlin-publish-plugin/build.yml?branch=main&logo=github)](https://github.com/joffrey-bion/gradle-kotlin-publish-plugin/actions/workflows/build.yml)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](https://github.com/joffrey-bion/gradle-kotlin-publish-plugin/blob/master/LICENSE)

A Gradle plugin to automatically setup publications for Kotlin projects.
This is mainly useful for multi-project builds where this logic must be shared.

This plugin automatically takes care of the following without any configuration:

 * applies the `maven-publish` plugin

 * configures the POM of all Maven publications so they have a `name` and `description` matching the project
   (these fields are required for Maven Central publication).

 * If the [Dokka plugin](https://github.com/Kotlin/dokka) is applied, this plugin sets up a `dokkaJar` task generating
  a javadoc jar which is added to all publications (the javadoc jar contains the Dokka HTML format, so it can work on
  other platforms than JVM).

 * If the [Github Info plugin](https://github.com/xvik/gradle-github-info-plugin) is applied (`ru.vyarus.github-info`)
   in addition to Dokka, this plugin configures source links in Dokka to point to the sources in the Github repository.

 * If the Kotlin/JVM plugin is applied, this plugin configures a Maven publication with classes and sources jar,
   so it's on par with Kotlin/Multiplatform subprojects (the KMP plugin configures this by default).

## Usage

Apply the plugin to your project using the `plugins` block in your `build.gradle(.kts)`:

```kotlin
plugins {
    id("org.hildan.kotlin-publish") version "<version>"
}
```

In multi-project builds, you should apply the plugin to every subproject for which you want to configure publications.
You can avoid repetition by using Gradle
[conventions plugins](https://docs.gradle.org/current/samples/sample_convention_plugins.html).

There is no configuration for this plugin, all its functionality is automatic.

## Example usage for Maven Central publication

In addition to what this plugin configures automatically, Maven Central requires:

* More information in the POM:
    * developer information
    * license (automatically handled by the [Github Info plugin](https://github.com/xvik/gradle-github-info-plugin))
    * SCM (automatically handled by the [Github Info plugin](https://github.com/xvik/gradle-github-info-plugin))
* Artifact signatures (can be done using the `signing` plugin)

Here is an example convention plugin that could be shared across subprojects:

`buildSrc/src/main/kotlin/myproject-publish.gradle.kts`:
```kotlin
plugins {
    id("org.hildan.kotlin-publish")
    id("org.jetbrains.dokka")
    id("ru.vyarus.github-info")
    signing
}

github {
   user = "joffrey-bion"
   license = "MIT"
}

// add the developer to every pom
publishing {
    // configureEach reacts on new publications being registered and configures them too
    publications.configureEach {
        if (this is MavenPublication) {
            pom {
                developers {
                    developer {
                        id.set("joffrey-bion")
                        name.set("Joffrey Bion")
                        email.set("joffrey.bion@gmail.com")
                    }
                }
            }
        }
    }
}

// sign publications to match Maven Central's expectations
signing {
    val signingKey: String? by project
    val signingPassword: String? by project
    useInMemoryPgpKeys(signingKey, signingPassword)
    sign(extensions.getByType<PublishingExtension>().publications)
}
```

This requires declaring the plugins as maven dependencies in `buildSrc/build.gradle.kts`:
```kotlin
plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    maven(url = "https://plugins.gradle.org/m2/")
}

dependencies {
    implementation(gradleApi())
    implementation(gradleKotlinDsl())
   
    // Replace versions here according to your needs
    implementation(kotlin("gradle-plugin", "1.7.20")) // the Kotlin version used in the project
    implementation("org.jetbrains.dokka:dokka-gradle-plugin:1.7.20")
    implementation("org.hildan.gradle:gradle-kotlin-publish-plugin:0.2.0")
    implementation("ru.vyarus:gradle-github-info-plugin:1.4.0")
}
```

And can then be used in subprojects like this:

```kotlin
plugins {
   kotlin("multiplatform") // or kotlin("jvm")
   id("myproject-publish")
}

// no extra config required, just the rest of your build script for this project
```

## Gradle compatibility

| Kotlin Publish Plugin | Min Gradle version |
|:---------------------:|:------------------:|
|         1.5.0         |        8.6         |
|         1.4.0         |        8.5         |
|         1.3.0         |        8.4         |
|         1.2.0         |        8.2         |
|         1.0.0         |        8.0         |
|         0.2.0         |        7.6         |
|         0.1.0         |       7.5.1        |

## License

This plugin is published under the MIT License.
