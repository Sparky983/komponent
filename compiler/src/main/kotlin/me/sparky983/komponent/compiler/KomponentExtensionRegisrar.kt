package me.sparky983.komponent.compiler

import org.jetbrains.kotlin.fir.extensions.FirExtensionApiInternals
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrar

@OptIn(FirExtensionApiInternals::class)
class KomponentExtensionRegisrar : FirExtensionRegistrar() {
    override fun ExtensionRegistrarContext.configurePlugin() {
        +::FirKomponentGenerator
    }
}