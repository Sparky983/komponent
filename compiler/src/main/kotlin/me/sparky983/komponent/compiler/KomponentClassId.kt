package me.sparky983.komponent.compiler

import org.jetbrains.kotlin.fir.types.ConeTypeProjection
import org.jetbrains.kotlin.fir.types.impl.ConeClassLikeTypeImpl
import org.jetbrains.kotlin.fir.types.toLookupTag
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

object KomponentClassId {
    val SIGNAL = ClassId(
        FqName("me.sparky983.komponent"),
        Name.identifier("Signal")
    )

    val COMPONENT = ClassId(
        FqName("me.sparky983.komponent"),
        Name.identifier("Component")
    )

    val ELEMENT = ClassId(
        FqName("me.sparky983.komponent"),
        Name.identifier("Element")
    )
}

fun ClassId.toConeKotlinType(vararg typeArguments: ConeTypeProjection) = ConeClassLikeTypeImpl(
    lookupTag = this.toLookupTag(),
    typeArguments = typeArguments,
    isMarkedNullable = false
)
