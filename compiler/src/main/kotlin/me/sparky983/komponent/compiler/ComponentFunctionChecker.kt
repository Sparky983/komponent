package me.sparky983.komponent.compiler

import org.jetbrains.kotlin.diagnostics.*
import org.jetbrains.kotlin.diagnostics.rendering.BaseDiagnosticRendererFactory
import org.jetbrains.kotlin.fir.analysis.checkers.MppCheckerKind
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirDeclarationChecker
import org.jetbrains.kotlin.fir.declarations.FirSimpleFunction
import org.jetbrains.kotlin.fir.expressions.UnresolvedExpressionTypeAccess
import org.jetbrains.kotlin.fir.types.classId
import org.jetbrains.kotlin.fir.types.toTypeProjection
import org.jetbrains.kotlin.name.StandardClassIds
import org.jetbrains.kotlin.types.Variance

object ComponentFunctionChecker : FirDeclarationChecker<FirSimpleFunction>(MppCheckerKind.Common) {
    private val ELEMENTS = StandardClassIds.Array.toConeClassLikeType(
        KomponentClassId.ELEMENT.toConeClassLikeType().toTypeProjection(Variance.OUT_VARIANCE)
    )

    @OptIn(UnresolvedExpressionTypeAccess::class)
    context(context: CheckerContext, reporter: DiagnosticReporter)
    override fun check(declaration: FirSimpleFunction) {
        val isComponent = declaration.annotations.any {
            it.coneTypeOrNull?.classId == KomponentClassId.COMPONENT
        }

        if (isComponent) {
            for (parameter in declaration.valueParameters) {
                if (parameter.isVararg &&
                    parameter.symbol.resolvedReturnType != ELEMENTS) {
                    reporter.reportOn(parameter.source, KomponentErrors.INCORRECTLY_TYPED_CHILDREN)
                }
            }
        }
    }
}

object KomponentErrors : KtDiagnosticsContainer() {
    // val INCORRECTLY_TYPED_CHILDREN by error0<PsiElement>(SourceElementPositioningStrategies.NAME_IDENTIFIER)
    // This is a hack to avoid creating two JARs as IntelliJ and the Kotlin compiler have different namespaces
    // as one of them relocates the compile
    val INCORRECTLY_TYPED_CHILDREN = KtDiagnosticFactory0(
        "INCORRECTLY_TYPED_CHILDREN",
        Severity.ERROR,
        SourceElementPositioningStrategies.NAME_IDENTIFIER,
        // Hack to avoid directly referencing PsiElement
        BackendErrors.NON_LOCAL_RETURN_IN_DISABLED_INLINE.psiType,
        getRendererFactory()
    )
    override fun getRendererFactory(): BaseDiagnosticRendererFactory {
        return DefaultKomponentErrorMessages
    }
}

object DefaultKomponentErrorMessages : BaseDiagnosticRendererFactory() {
    override val MAP: KtDiagnosticFactoryToRendererMap by KtDiagnosticFactoryToRendererMap("Komponent") {
        it.put(KomponentErrors.INCORRECTLY_TYPED_CHILDREN, "Children must have type " + KomponentClassId.ELEMENT)
    }
}
