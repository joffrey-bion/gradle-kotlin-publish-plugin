package org.hildan.kotlin.publish

import com.vanniktech.maven.publish.MavenPublishPlugin
import org.gradle.api.*
import org.gradle.api.provider.*
import org.gradle.api.publish.*
import org.gradle.api.publish.maven.*
import org.gradle.kotlin.dsl.*
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.dokka.gradle.*
import ru.vyarus.gradle.plugin.github.GithubInfoExtension
import java.io.*
import java.net.*

/**
 * Applies the Gradle Maven Publish Plugin by vanniktech (not the default maven-publish from Gradle) and fixes up a
 * couple nissing.
 *
 *  * If the Github Info plugin ("ru.vyarus.github-info") and the Dokka plugin are both applied,
 *    this plugin configures source links in Dokka to point to the GitHub repository
 */
class KotlinPublishPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.apply<MavenPublishPlugin>()

        // the vanniktech plugin doesn't have defaults for name and description, which are necessary for Maven Central
        project.configure<PublishingExtension> {
            // configureEach reacts on new publications being registered and configures them too
            publications.configureEach {
                if (this is MavenPublication) {
                    pom {
                        // using providers because the name and description can be set after application of the plugin
                        if (!name.isPresent) {
                            name.set(project.provider { project.name })
                        }
                        if (!description.isPresent) {
                            description.set(project.provider { project.description })
                        }
                    }
                }
            }
        }

        project.pluginManager.withPlugin("org.jetbrains.dokka") {
            project.pluginManager.withPlugin("ru.vyarus.github-info") {
                val repoUrl = project.provider { project.extensions.getByType<GithubInfoExtension>().repositoryUrl }
                project.configureDokkaSourceLinksToGithub(repoUrl)
            }
        }
    }
}

private fun Project.configureDokkaSourceLinksToGithub(githubRepoUrl: Provider<String>) {
    val dokkaPluginMode = project.findProperty("org.jetbrains.dokka.experimental.gradle.pluginMode")?.toString()
    if (dokkaPluginMode in setOf("V2Enabled", "V2EnabledWithHelpers")) {
        extensions.configure<DokkaExtension> {
            dokkaSourceSets {
                configureEach {
                    sourceRoots.forEach { sourceRootDir ->
                        val sourceRootRelativePath = sourceRootDir.relativeTo(rootProject.projectDir).toSlashSeparatedString()
                        sourceLink {
                            localDirectory.set(sourceRootDir)
                            // HEAD points to the default branch of the repo.
                            remoteUrl.set(githubRepoUrl.map { URI("$it/blob/HEAD/$sourceRootRelativePath") })
                        }
                    }
                }
            }
        }
    } else {
        // I know, and no, we cannot remove duplication here. These properties / functions have the same names, but
        // they are not on the same type, so they are different from the Dokka v2 block.
        tasks.withType<AbstractDokkaLeafTask>().configureEach {
            dokkaSourceSets {
                configureEach {
                    sourceRoots.forEach { sourceRootDir ->
                        val sourceRootRelativePath =
                            sourceRootDir.relativeTo(rootProject.projectDir).toSlashSeparatedString()
                        sourceLink {
                            localDirectory.set(sourceRootDir)
                            // HEAD points to the default branch of the repo.
                            remoteUrl.set(githubRepoUrl.map { URI("$it/blob/HEAD/$sourceRootRelativePath").toURL() })
                        }
                    }
                }
            }
        }
    }
}

// ensures slash separator even on Windows, useful for URLs creation
private fun File.toSlashSeparatedString(): String = toPath().joinToString("/")
