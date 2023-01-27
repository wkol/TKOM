package org.example.interpreter.utils

import org.example.parser.data.RuntimeValue
import org.example.parser.data.Type


fun RuntimeValue.isNonNullableNumber(): Boolean =
    !type.isNullable && (type.type == Type.TypePrimitive.INT || type.type == Type.TypePrimitive.DOUBLE)


fun RuntimeValue.canBeAssignedTo(otherType: Type?): Boolean {
    return (type.type == otherType?.type || otherType?.isNullable == true && type.type == Type.TypePrimitive.VOID)
            && (!type.isNullable || otherType.isNullable)
}

object TypeConst {
    val INT = Type(
        type = Type.TypePrimitive.INT,
        isNullable = false
    )
    val DOUBLE = Type(
        type = Type.TypePrimitive.DOUBLE,
        isNullable = false
    )
    val STRING = Type(
        type = Type.TypePrimitive.STRING,
        isNullable = false
    )
    val BOOL = Type(
        type = Type.TypePrimitive.BOOL,
        isNullable = false
    )
    val VOID = Type(
        type = Type.TypePrimitive.VOID,
        isNullable = true
    )

    val INT_NULLABLE = Type(
        type = Type.TypePrimitive.INT,
        isNullable = true
    )

    val DOUBLE_NULLABLE = Type(
        type = Type.TypePrimitive.DOUBLE,
        isNullable = true
    )

    val STRING_NULLABLE = Type(
        type = Type.TypePrimitive.STRING,
        isNullable = true
    )

    val BOOL_NULLABLE = Type(
        type = Type.TypePrimitive.BOOL,
        isNullable = true
    )
}
