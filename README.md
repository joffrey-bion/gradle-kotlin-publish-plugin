# Kotlin Publish Plugin

[![Gradle plugin version](https://img.shields.io/maven-metadata/v/https/plugins.gradle.org/m2/org/hildan/kotlin/publish/org.hildan.kotlin.publish/maven-metadata.xml.svg?label=gradle&logo=gradle)](https://plugins.gradle.org/plugin/org.hildan.kotlin.publish)
[![Github Build](https://img.shields.io/github/workflow/status/joffrey-bion/gradle-kotlin-publish-plugin/CI%20Build?label=build&logo=github)](https://github.com/joffrey-bion/gradle-kotlin-publish-plugin/actions?query=workflow%3A%22CI+Build%22)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](https://github.com/joffrey-bion/gradle-kotlin-publish-plugin/blob/master/LICENSE)

A Gradle plugin to automatically setup source and javadoc jar publications for Kotlin projects.

This plugin is meant to react to other plugins:

 * If the Kotlin/JVM plugin is applied, this plugin configures a Maven publication with code and sources jar
   so it's on par with Kotlin/MPP (which does it by default)
 * If the Dokka plugin is applied, this plugin sets up a `dokkaJar` task generating a javadoc jar which is added to all
   publications (the javadoc jar contains the Dokka HTML format, so it can work on other platforms than JVM)
 * If the [Github Info plugin](https://github.com/xvik/gradle-github-info-plugin) is applied (`ru.vyarus.github-info`)
   in addition to Dokka, this plugin configures source links in Dokka to point to the sources in the Github repository.

This plugin also configures the POM of all Maven publications in the project so they have a name and description
matching the project.

## Usage

Apply the plugin to your project using the `plugins` block in your `build.gradle(.kts)`:

```kotlin
plugins {
    id("org.hildan.kotlin.publish") version "<version>"
}
```

In multi-project builds, you should apply the plugin to every subproject for which you want to configure publications.
You can avoid repetition by using Gradle
[conventions plugins](https://docs.gradle.org/current/samples/sample_convention_plugins.html).

There is no configuration for this plugin, all its functionality is automatic.

## License

This plugin is published under the MIT License.
