package me.sparky983.komponent.gradle

import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption

class KomponentPlugin : KotlinCompilerPluginSupportPlugin {
    private companion object {
        const val GROUP_NAME = "me.sparky983.komponent"
        const val COMPILER_ARTIFACT_ID = "compiler"
        const val API_ARTIFACT_ID = "komponent"
        val VERSION = KomponentPlugin::class.java.`package`.implementationVersion!!
    }

    override fun applyToCompilation(compilation: KotlinCompilation<*>): Provider<List<SubpluginOption>> {
        compilation.dependencies {
            implementation("$GROUP_NAME:$API_ARTIFACT_ID:$VERSION")
        }
        return compilation.target.project.provider { listOf() }
    }

    override fun getCompilerPluginId() = "me.sparky983.komponent"

    override fun isApplicable(compilation: KotlinCompilation<*>) = true

    override fun getPluginArtifact(): SubpluginArtifact = SubpluginArtifact(
        groupId = GROUP_NAME,
        artifactId = COMPILER_ARTIFACT_ID,
        version = VERSION
    )
}