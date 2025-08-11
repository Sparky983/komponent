package me.sparky983.komponent.compiler

import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.DeclarationCheckers
import org.jetbrains.kotlin.fir.analysis.extensions.FirAdditionalCheckersExtension

class KomponentCheckers(session: FirSession) : FirAdditionalCheckersExtension(session) {
    override val declarationCheckers = object : DeclarationCheckers() {
        override val simpleFunctionCheckers = setOf(ComponentFunctionChecker)
    }
}
