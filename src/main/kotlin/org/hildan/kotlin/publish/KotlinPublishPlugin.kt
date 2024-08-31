package org.hildan.kotlin.publish

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.*
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.*
import org.gradle.plugins.signing.*
import org.jetbrains.dokka.gradle.*
import ru.vyarus.gradle.plugin.github.GithubInfoExtension
import java.io.File
import java.net.URL

/**
 * Applies the Maven Publish plugin and sets up Kotlin publications.
 *
 *  * If the Kotlin/JVM or Kotlin/JS plugin is applied, this plugin configures publications with sources jar
 *    so it's on par with Kotlin/MPP
 *  * If the Dokka plugin is applied, this plugin sets up a dokkaJar task generating a javadoc jar added to all
 *    publications (the javadoc jar contains the Dokka HTML format, so it can work on other platforms than JVM)
 *  * If the Github Info plugin is applied ("ru.vyarus.github-info") in addition to Dokka, this plugin configures
 *    source links in Dokka to point to the Github repository
 */
class KotlinPublishPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.apply<MavenPublishPlugin>()

        // Publications are not automatically configured with Kotlin JVM plugin, but they are with MPP.
        // We align here so both MPP and JVM projects have publications with sources jar setup.
        project.pluginManager.withPlugin("org.jetbrains.kotlin.jvm") {
            // sourcesJar is not added by default by the Kotlin JVM plugin
            project.configure<JavaPluginExtension>() {
                withSourcesJar()
            }

            project.configure<PublishingExtension> {
                publications {
                    create<MavenPublication>("maven") {
                        from(project.components["java"]) // java components have the sourcesJar AND the Kotlin artifacts
                    }
                }
            }
        }

        project.pluginManager.withPlugin("org.jetbrains.kotlin.multiplatform") {
            fixOverlappingOutputsForSigningTask(project)
        }

        project.pluginManager.withPlugin("org.jetbrains.dokka") {
            configureDokka(project)
        }

        project.configure<PublishingExtension> {
            // configureEach reacts on new publications being registered and configures them too
            publications.configureEach {
                if (this is MavenPublication) {
                    pom {
                        // using providers because the name and description can be set after application of the plugin
                        name.set(project.provider { project.name })
                        description.set(project.provider { project.description })
                    }
                }
            }
        }
    }
}

// Resolves issues with .asc task output of the sign task of native targets.
// See: https://github.com/gradle/gradle/issues/26132
// And: https://youtrack.jetbrains.com/issue/KT-46466
private fun fixOverlappingOutputsForSigningTask(project: Project) {
    project.tasks.withType<Sign>().configureEach {
        val pubName = name.removePrefix("sign").removeSuffix("Publication")

        // These tasks only exist for native targets, hence findByName() to avoid trying to find them for other targets

        // Task ':linkDebugTest<platform>' uses this output of task ':sign<platform>Publication' without declaring an explicit or implicit dependency
        project.tasks.findByName("linkDebugTest$pubName")?.let {
            mustRunAfter(it)
        }
        // Task ':compileTestKotlin<platform>' uses this output of task ':sign<platform>Publication' without declaring an explicit or implicit dependency
        project.tasks.findByName("compileTestKotlin$pubName")?.let {
            mustRunAfter(it)
        }
    }
}

private fun configureDokka(project: Project) {
    project.configure<PublishingExtension> {
        // configureEach reacts on new publications being registered and configures them too
        publications.configureEach {
            if (this is MavenPublication) {
                val publication = this
                val dokkaJar = project.tasks.register("${publication.name}DokkaJar", Jar::class) {
                    group = JavaBasePlugin.DOCUMENTATION_GROUP
                    description = "Assembles Kotlin docs with Dokka into a Javadoc jar"
                    archiveClassifier.set("javadoc")
                    from(project.tasks.named("dokkaHtml"))

                    // Each archive name should be distinct, to avoid implicit dependency issues.
                    // We use the same format as the sources Jar tasks.
                    // https://youtrack.jetbrains.com/issue/KT-46466
                    archiveBaseName.set("${archiveBaseName.get()}-${publication.name}")
                }
                artifact(dokkaJar)
            }
        }
    }

    project.pluginManager.withPlugin("ru.vyarus.github-info") {
        val github = project.extensions.getByType<GithubInfoExtension>()
        val repoUrl = github.repositoryUrl
        project.configureDokkaSourceLinksToGithub(repoUrl)
    }
}

private fun Project.configureDokkaSourceLinksToGithub(githubRepoUrl: String) {
    // HEAD points to the default branch of the repo.
    // The trailing slash is important for the call to URL(parent, child) below
    val repoBlobBaseUrl = URL("$githubRepoUrl/blob/HEAD/")

    tasks.withType<AbstractDokkaLeafTask>().configureEach {
        dokkaSourceSets {
            configureEach {
                sourceRoots.forEach { sourceRootDir ->
                    val sourceRootRelativePath = sourceRootDir.relativeTo(rootProject.projectDir).toSlashSeparatedString()
                    sourceLink {
                        localDirectory.set(sourceRootDir)
                        remoteUrl.set(URL(repoBlobBaseUrl, sourceRootRelativePath))
                    }
                }
            }
        }
    }
}

// ensures slash separator even on Windows, useful for URLs creation
private fun File.toSlashSeparatedString(): String = toPath().joinToString("/")
