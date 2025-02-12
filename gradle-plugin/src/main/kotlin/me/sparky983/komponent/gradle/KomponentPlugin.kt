package me.sparky983.komponent.gradle

import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption

class KomponentPlugin : KotlinCompilerPluginSupportPlugin {
    private companion object {
        const val GROUP_NAME = "me.sparky983.komponent"
        const val ARTIFACT_NAME = "compiler"
        const val VERSION_NUMBER = "0.1.0"
    }
    
    override fun applyToCompilation(compilation: KotlinCompilation<*>): Provider<List<SubpluginOption>> {
        return compilation.target.project.provider { listOf() }
    }

    override fun apply(target: Project) {
        super.apply(target)
    }

    override fun getCompilerPluginId(): String = "komponent"

    override fun isApplicable(compilation: KotlinCompilation<*>): Boolean {
        return true
    }

    override fun getPluginArtifact(): SubpluginArtifact = SubpluginArtifact(
        groupId = GROUP_NAME,
        artifactId = ARTIFACT_NAME,
        version = VERSION_NUMBER
    )
}