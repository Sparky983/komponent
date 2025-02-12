package me.sparky983.komponent.compiler

import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CliOption
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.config.CompilerConfiguration

class CompilerCommandLineProcessor : CommandLineProcessor {
    override val pluginId = "komponent"
    
    override val pluginOptions: Collection<CliOption> = listOf()

    override fun processOption(
        option: AbstractCliOption,
        value: String,
        configuration: CompilerConfiguration
    ) {
        error("Unexpected option ${option.optionName}")
    }
}