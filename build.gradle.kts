plugins {
    `kotlin-dsl`
    id("com.gradle.plugin-publish") version "1.1.0"
    id("org.hildan.github.changelog") version "1.12.1"
    id("ru.vyarus.github-info") version "1.4.0"
}

group = "org.hildan.gradle"
description = "A Gradle plugin to automatically setup source and javadoc jar publications for Kotlin projects"

github {
    user = "joffrey-bion"
    license = "MIT"
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(kotlin("gradle-plugin", "1.8.0"))
    compileOnly("org.jetbrains.dokka:dokka-gradle-plugin:1.7.20")
    compileOnly("ru.vyarus:gradle-github-info-plugin:1.4.0")
}

pluginBundle {
    tags = listOf("kotlin", "publish", "javadoc", "dokka", "kdoc", "github", "maven")
}

gradlePlugin {
    plugins {
        create("kotlinPublishPlugin") {
            id = "org.hildan.kotlin-publish"
            displayName = "Kotlin Publish Plugin"
            description = "Configures sources and Javadoc (KDoc) jars for Kotlin JVM and MPP projects"
            implementationClass = "org.hildan.kotlin.publish.KotlinPublishPlugin"
        }
    }
}

changelog {
    githubUser = github.user
    futureVersionTag = project.version.toString()
}
