package me.sparky983.komponent.compiler

import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.FirValueParameter
import org.jetbrains.kotlin.fir.expressions.FirExpression
import org.jetbrains.kotlin.fir.expressions.FirFunctionCall
import org.jetbrains.kotlin.fir.expressions.buildResolvedArgumentList
import org.jetbrains.kotlin.fir.expressions.builder.buildFunctionCall
import org.jetbrains.kotlin.fir.expressions.resolvedArgumentMapping
import org.jetbrains.kotlin.fir.extensions.FirExtensionApiInternals
import org.jetbrains.kotlin.fir.extensions.FirFunctionCallRefinementExtension
import org.jetbrains.kotlin.fir.references.FirResolvedErrorReference
import org.jetbrains.kotlin.fir.references.builder.buildResolvedNamedReference
import org.jetbrains.kotlin.fir.resolve.calls.ArgumentTypeMismatch
import org.jetbrains.kotlin.fir.resolve.calls.candidate.CallInfo
import org.jetbrains.kotlin.fir.resolve.diagnostics.ConeInapplicableCandidateError
import org.jetbrains.kotlin.fir.resolve.providers.symbolProvider
import org.jetbrains.kotlin.fir.symbols.SymbolInternals
import org.jetbrains.kotlin.fir.symbols.impl.ConeClassLikeLookupTagImpl
import org.jetbrains.kotlin.fir.symbols.impl.FirNamedFunctionSymbol
import org.jetbrains.kotlin.fir.types.ConeClassLikeType
import org.jetbrains.kotlin.fir.types.classId
import org.jetbrains.kotlin.fir.types.impl.ConeClassLikeTypeImpl
import org.jetbrains.kotlin.fir.types.isSubtypeOf
import org.jetbrains.kotlin.fir.types.resolvedType
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

/**
 * Rewrites regular arguments to be applicable for `Signal` parameters.
 */
@OptIn(FirExtensionApiInternals::class)
class FirKomponentGenerator(session: FirSession) :
    FirFunctionCallRefinementExtension(session) {
    private companion object {
        val SIGNAL_CLASS_ID = ClassId(
            FqName("me.sparky983.komponent"),
            Name.identifier("Signal")
        )
    }
    
    @OptIn(SymbolInternals::class)
    override fun intercept(
        callInfo: CallInfo,
        symbol: FirNamedFunctionSymbol
    ): CallReturnType? {
        for (parameter in symbol.valueParameterSymbols) {
            val type = parameter.resolvedReturnType
            if (type is ConeClassLikeType
                && type.lookupTag.classId == SIGNAL_CLASS_ID) {
                return CallReturnType(symbol.resolvedReturnTypeRef)
            }
        }
        return null
    }

    @OptIn(SymbolInternals::class)
    override fun transform(
        call: FirFunctionCall,
        originalSymbol: FirNamedFunctionSymbol
    ): FirFunctionCall {
        /*
         * Short rundown of transformation steps:
         * 1. Filter all diagnostics for type mismatches
         * 2. Try to rewrite type mismatches as `Signal.just` calls
         * 3. Check to see if there are still any errors
         * 
         * If any of these steps fail, the original call is returned, retaining
         * the same diagnostics.
         */

        val calleeReference = call.calleeReference
        if (calleeReference !is FirResolvedErrorReference) {
            // Ensures guarantee of backwards compatability
            return buildFunctionCall {
                this.coneTypeOrNull = originalSymbol.resolvedReturnType
                this.typeArguments.addAll(call.typeArguments)
                this.dispatchReceiver = call.dispatchReceiver
                this.extensionReceiver = call.extensionReceiver
                this.explicitReceiver = call.explicitReceiver
                this.argumentList = call.argumentList
                this.source = call.source
                this.calleeReference = buildResolvedNamedReference {
                    this.source = call.calleeReference.source
                    this.name = originalSymbol.name
                    this.resolvedSymbol = originalSymbol
                }
            }
        }
        
        val error = calleeReference.diagnostic
        
        if (error !is ConeInapplicableCandidateError) {
            return call
        }
        
        val candidate = error.candidate
        val diagnostics = candidate.diagnostics
        
        val remainingDiagnostics = diagnostics
            .filterIsInstance<ArgumentTypeMismatch>()
            .map { it.argument }
            .toMutableList()
        
        if (remainingDiagnostics.size != diagnostics.size) {
            // There are diagnostics that aren't type mismatches.
            // TODO: remove diagnostics that would've been resolved
            return call
        }

        val arguments = LinkedHashMap<FirExpression, FirValueParameter>().apply {
            call.resolvedArgumentMapping!!.forEach { (expression, parameter) ->
                val paramType = parameter.symbol.resolvedReturnType
                val argType = expression.resolvedType
                if (paramType is ConeClassLikeType
                    && paramType.classId == SIGNAL_CLASS_ID
                    && !argType.isSubtypeOf(paramType, session)
                    && ConeClassLikeTypeImpl(
                        ConeClassLikeLookupTagImpl(SIGNAL_CLASS_ID),
                        arrayOf(argType),
                        false
                    ).isSubtypeOf(paramType, session)
                ) {
                    remainingDiagnostics.remove(expression)
                    val just = buildFunctionCall {
                        this.coneTypeOrNull = paramType
                        val factory = session.symbolProvider
                            .getTopLevelFunctionSymbols(
                                FqName("me.sparky983.komponent"),
                                Name.identifier("just")
                            )
                            .single()
                        this.calleeReference = buildResolvedNamedReference {
                            this.name = Name.identifier("just")
                            this.resolvedSymbol = factory
                        }
                        this.argumentList = buildResolvedArgumentList(
                            null,
                            linkedMapOf(expression to factory.valueParameterSymbols.single().fir)
                        )
                    }
                    put(just, parameter)
                } else {
                    put(expression, parameter)
                }
            }
        }
        
        if (!remainingDiagnostics.isEmpty()) {
            return call
        }

        return buildFunctionCall {
            this.coneTypeOrNull = originalSymbol.resolvedReturnType
            this.typeArguments.addAll(call.typeArguments)
            this.dispatchReceiver = call.dispatchReceiver
            this.extensionReceiver = call.extensionReceiver
            this.explicitReceiver = call.explicitReceiver
            this.argumentList = buildResolvedArgumentList(
                call.argumentList,
                arguments
            )
            this.source = call.source
            this.calleeReference = buildResolvedNamedReference {
                this.source = call.calleeReference.source
                this.name = originalSymbol.name
                this.resolvedSymbol = originalSymbol
            }
        }
    }
}