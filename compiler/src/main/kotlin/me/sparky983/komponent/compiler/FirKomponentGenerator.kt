package me.sparky983.komponent.compiler

import org.jetbrains.kotlin.KtFakeSourceElementKind
import org.jetbrains.kotlin.KtSourceElement
import org.jetbrains.kotlin.fakeElement
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.FirValueParameter
import org.jetbrains.kotlin.fir.declarations.hasAnnotation
import org.jetbrains.kotlin.fir.diagnostics.ConeDiagnostic
import org.jetbrains.kotlin.fir.expressions.*
import org.jetbrains.kotlin.fir.expressions.builder.buildFunctionCall
import org.jetbrains.kotlin.fir.expressions.builder.buildVarargArgumentsExpression
import org.jetbrains.kotlin.fir.expressions.impl.FirResolvedArgumentList
import org.jetbrains.kotlin.fir.extensions.FirExtensionApiInternals
import org.jetbrains.kotlin.fir.extensions.FirFunctionCallRefinementExtension
import org.jetbrains.kotlin.fir.references.FirNamedReference
import org.jetbrains.kotlin.fir.references.FirResolvedErrorReference
import org.jetbrains.kotlin.fir.references.builder.buildResolvedErrorReference
import org.jetbrains.kotlin.fir.references.builder.buildResolvedNamedReference
import org.jetbrains.kotlin.fir.resolve.calls.ArgumentTypeMismatch
import org.jetbrains.kotlin.fir.resolve.calls.MixingNamedAndPositionArguments
import org.jetbrains.kotlin.fir.resolve.calls.NonVarargSpread
import org.jetbrains.kotlin.fir.resolve.calls.ResolutionDiagnostic
import org.jetbrains.kotlin.fir.resolve.calls.TooManyArguments
import org.jetbrains.kotlin.fir.resolve.calls.candidate.CallInfo
import org.jetbrains.kotlin.fir.resolve.diagnostics.ConeInapplicableCandidateError
import org.jetbrains.kotlin.fir.resolve.providers.symbolProvider
import org.jetbrains.kotlin.fir.symbols.SymbolInternals
import org.jetbrains.kotlin.fir.symbols.impl.FirNamedFunctionSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirRegularClassSymbol
import org.jetbrains.kotlin.fir.types.*
import org.jetbrains.kotlin.fir.types.impl.ConeClassLikeTypeImpl
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.StandardClassIds

/**
 * Rewrites regular arguments to be applicable for `Signal` parameters.
 */
@OptIn(FirExtensionApiInternals::class)
class FirKomponentGenerator(session: FirSession) :
    FirFunctionCallRefinementExtension(session) {
    @OptIn(SymbolInternals::class)
    override fun intercept(
        callInfo: CallInfo,
        symbol: FirNamedFunctionSymbol
    ): CallReturnType? {
        if (symbol.hasAnnotation(KomponentClassId.COMPONENT, session)) {
            return CallReturnType(symbol.resolvedReturnTypeRef)
        }
        return null
    }

    @OptIn(SymbolInternals::class)
    override fun transform(
        call: FirFunctionCall,
        originalSymbol: FirNamedFunctionSymbol
    ): FirFunctionCall {
        val calleeReference = call.calleeReference
        if (calleeReference !is FirResolvedErrorReference) {
            // Ensures guarantee of backwards compatability
            return replaceFunctionCall(call, calleeReference = buildResolvedNamedReference {
                this.source = calleeReference.source
                this.name = calleeReference.name
                this.resolvedSymbol = originalSymbol
            })
        }
        
        val error = calleeReference.diagnostic

        if (error !is ConeInapplicableCandidateError) {
            return call
        }
        
        val candidate = error.candidate
        val diagnostics = candidate.diagnostics.toMutableList()

        diagnostics.removeIf { it is MixingNamedAndPositionArguments }

        fun MutableList<ResolutionDiagnostic>.removeArgumentTypeMismatchFor(expression: FirExpression) {
            // When doing a vararg spread where the argument would originally be passed to a
            // non-child parameter, the type diagnostic's argument is the inner expression of the
            // spread expression.
            // TODO: are all type diagnostics on the wrapped expressions
            var unwrappedExpression = expression
            while (unwrappedExpression is FirWrappedExpression) {
                unwrappedExpression = unwrappedExpression.expression
            }
            this.removeIf { it is ArgumentTypeMismatch && it.argument == unwrappedExpression }
        }

        val arguments = LinkedHashMap<FirExpression, FirValueParameter>()
        val invalidArguments = mutableSetOf<FirExpression>()

        /*
         * Short rundown of Signal argument transformation steps:
         * 1. Try to rewrite type mismatches as `Signal.just` calls, removing fixed errors
         * 2. Transform all positional arguments into varargs, removing caused errors
         * 3. Check to see if there are still any errors, if so, the function call was invalid for
         * other reasons
         */
        for ((expression, parameter) in call.resolvedArgumentMapping!!) {
            val paramType = parameter.symbol.resolvedReturnType
            val argType = expression.resolvedType
            if (paramType is ConeClassLikeType
                && paramType.classId == KomponentClassId.SIGNAL
                && !argType.isSubtypeOf(paramType, session)
                && KomponentClassId.SIGNAL.toConeClassLikeType(argType).isSubtypeOf(paramType, session)
            ) {
                diagnostics.removeArgumentTypeMismatchFor(expression)
                arguments[createJustCall(paramType, value = expression)] = parameter
            } else {
                arguments[expression] = parameter
            }
        }

        val argumentList = call.argumentList as FirResolvedArgumentList
        val originalArgumentList = argumentList.originalArgumentList!!

        val positionalArguments = mutableListOf<FirExpression>()
        var encounteredPositional = false
        for (argument in originalArgumentList.arguments) {
            if (argument is FirNamedArgumentExpression) {
                if (encounteredPositional) {
                    diagnostics.add(MixingNamedAndPositionArguments(argument))
                }
            } else {
                positionalArguments.add(argument)
                encounteredPositional = true
            }
        }

        val vararg = originalSymbol.valueParameterSymbols.singleOrNull { it.isVararg }

        if (vararg != null) {
            val children = mutableListOf<FirExpression>()

            val childrenType = vararg.resolvedReturnType
            val elementType = childrenType.typeArguments.firstOrNull()?.type
                ?: StandardClassIds.elementTypeByPrimitiveArrayType[childrenType.classId]?.toConeClassLikeType()
                ?: StandardClassIds.elementTypeByUnsignedArrayType[childrenType.classId]?.toConeClassLikeType()
            elementType!! // There is no other type a varargs argument can be right?

            val namedChildrenArgument = originalArgumentList
                .arguments
                .firstOrNull { it is FirNamedArgumentExpression && it.name == vararg.name}

            if (namedChildrenArgument != null && positionalArguments.isNotEmpty()) {
                for (argument in positionalArguments) {
                    diagnostics.add(TooManyArguments(argument, originalSymbol.fir))
                }
            } else {
                for (argument in positionalArguments) {
                    /*
                     * How arguments are resolved:
                     * 1. If an argument is positional, it is automatically put into the children
                     * varargs
                     * 2. Otherwise, it is a "prop" and is passed in like normal
                     */
                    if (StandardClassIds.Array.toConeClassLikeType(argument.resolvedType).isSubtypeOf(childrenType, session) ||
                        (argument is FirSpreadArgumentExpression
                            && argument.resolvedType.isSubtypeOf(childrenType, session))) {
                        children.add(argument)
                        arguments.remove(argument)
                        diagnostics.removeArgumentTypeMismatchFor(argument)
                        diagnostics.removeIf { it is NonVarargSpread && it.argument == argument }
                    } else {
                        // invalidArguments.add(argument)
                        // this is sometimes redundant/not even correct if the argument is a vararg because it's wrapped in a vararg argument thing
                        // we should see what happens when we add it anyway and/or check if its not a vararg
                        if (diagnostics.none { it is ArgumentTypeMismatch && it.argument == argument }) {
                            // No type mismatch can occur if the parameter is an extra parameter, mix of positional and named, etc.
                            diagnostics.add(ArgumentTypeMismatch(
                                expectedType = elementType,
                                actualType = argument.resolvedType,
                                argument = argument,
                                isMismatchDueToNullability = false // TODO: check this
                            ))
                        }
                    }
                }

                if (diagnostics.isEmpty() && elementType.classId == KomponentClassId.ELEMENT) {
                    val childrenExpression = buildVarargArgumentsExpression {
                        val first = children.firstOrNull()?.source
                        val last = children.lastOrNull()?.source
                        if (first != null && last != null) {
                            this.source = first.fakeElement(
                                KtFakeSourceElementKind.VarargArgument,
                                first.startOffset,
                                last.endOffset
                            )
                        }
                        this.coneTypeOrNull = childrenType
                        this.coneElementTypeOrNull = elementType
                        this.arguments += children
                    }
                    arguments.entries.removeIf { (argument, parameter) ->
                        if (argument is FirVarargArgumentsExpression) {
                            assert(parameter.symbol == vararg)
                            true
                        } else {
                            false
                        }
                    }
                    arguments[childrenExpression] = vararg.fir
                }
            }
        }

        if (diagnostics.isNotEmpty()) {
            val actualDiagnostics = (candidate.diagnostics as MutableList<ResolutionDiagnostic>)
            actualDiagnostics.clear()
            actualDiagnostics += diagnostics
            return replaceFunctionCall(
                call,
                argumentList = buildArgumentListForErrorCall(
                    originalArgumentList,
                    LinkedHashMap<FirExpression, FirValueParameter?>(arguments)
                        .also { it += invalidArguments.associateWith { null } }
                ),
                calleeReference = buildResolvedErrorReference {
                    this.source = calleeReference.source
                    this.name = calleeReference.name
                    this.resolvedSymbol = originalSymbol
                    this.diagnostic = ConeInapplicableCandidateError(
                        applicability = error.applicability,
                        candidate = candidate
                    )
                }
            )
        }

        return replaceFunctionCall(
            call,
            argumentList = buildResolvedArgumentList(
                originalArgumentList,
                arguments
            ),
            calleeReference = buildResolvedNamedReference {
                this.source = calleeReference.source
                this.name = calleeReference.name
                this.resolvedSymbol = originalSymbol
            }
        )
    }

    override fun ownsSymbol(symbol: FirRegularClassSymbol): Boolean {
        // we don't own any symbols since we don't generate new ones
        return false
    }

    override fun anchorElement(symbol: FirRegularClassSymbol): KtSourceElement {
        // transformed element has the same source as the original element
        return symbol.source!!
    }

    override fun restoreSymbol(
        call: FirFunctionCall,
        name: Name
    ): FirRegularClassSymbol? {
        // not sure what this is - i think return generated class by name for given function call?
        return null
    }

    @OptIn(SymbolInternals::class)
    private fun createJustCall(type: ConeKotlinType, value: FirExpression): FirFunctionCall {
        return buildFunctionCall {
            this.coneTypeOrNull = type
            this.source = value.source // TODO: fake source
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
                linkedMapOf(value to factory.valueParameterSymbols.single().fir)
            )
        }
    }

    @OptIn(UnresolvedExpressionTypeAccess::class)
    private fun replaceFunctionCall(
        call: FirFunctionCall,
        coneTypeOrNull: ConeKotlinType? = call.coneTypeOrNull,
        typeArguments: List<FirTypeProjection> = call.typeArguments,
        explicitReceiver: FirExpression? = call.explicitReceiver,
        dispatchReceiver: FirExpression? = call.dispatchReceiver,
        extensionReceiver: FirExpression? = call.extensionReceiver,
        source: KtSourceElement? = call.source,
        nonFatalDiagnostics: List<ConeDiagnostic> = call.nonFatalDiagnostics,
        argumentList: FirArgumentList = call.argumentList,
        calleeReference: FirNamedReference = call.calleeReference,
        origin: FirFunctionCallOrigin = call.origin
    ): FirFunctionCall {
        return buildFunctionCall {
            this.coneTypeOrNull = coneTypeOrNull
            this.typeArguments += typeArguments
            this.dispatchReceiver = dispatchReceiver
            this.extensionReceiver = extensionReceiver
            this.explicitReceiver = explicitReceiver
            this.argumentList = argumentList
            this.source = source
            this.nonFatalDiagnostics += nonFatalDiagnostics
            this.calleeReference = calleeReference
            this.origin = origin
        }
    }
}
