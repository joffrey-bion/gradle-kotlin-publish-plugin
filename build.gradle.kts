plugins {
    `kotlin-dsl`
    id("com.gradle.plugin-publish") version "1.2.1"
    id("org.hildan.github.changelog") version "2.2.0"
    id("ru.vyarus.github-info") version "1.5.0"
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
    compileOnly("org.jetbrains.dokka:dokka-gradle-plugin:1.9.20")
    compileOnly("ru.vyarus:gradle-github-info-plugin:1.5.0")
}

gradlePlugin {
    plugins {
        @Suppress("UnstableApiUsage")
        create("kotlinPublishPlugin") {
            id = "org.hildan.kotlin-publish"
            displayName = "Kotlin Publish Plugin"
            description = "Configures sources and Javadoc (KDoc) jars for Kotlin JVM, JS and MPP projects"
            implementationClass = "org.hildan.kotlin.publish.KotlinPublishPlugin"
            website = "https://github.com/joffrey-bion/gradle-kotlin-publish-plugin"
            vcsUrl = "https://github.com/joffrey-bion/gradle-kotlin-publish-plugin"
            tags = listOf("kotlin", "publish", "javadoc", "dokka", "kdoc", "github", "maven")
        }
    }
}

changelog {
    githubUser = github.user
    futureVersionTag = project.version.toString()
}
