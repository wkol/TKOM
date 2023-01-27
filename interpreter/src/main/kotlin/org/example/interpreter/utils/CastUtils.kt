package org.example.interpreter.utils

import org.example.parser.data.RuntimeValue
import org.example.parser.data.Type


fun RuntimeValue.castType(type: Type): RuntimeValue? {
    return when (type.type) {
        Type.TypePrimitive.INT -> castToInt(this, type.isNullable)
        Type.TypePrimitive.DOUBLE -> castToDouble(this, type.isNullable)
        Type.TypePrimitive.STRING -> castToString(this, type.isNullable)
        Type.TypePrimitive.BOOL -> castToBool(this, type.isNullable)
        Type.TypePrimitive.VOID -> RuntimeValue(null, type)
    }
}

private fun castToString(value: RuntimeValue, isNullable: Boolean): RuntimeValue {
    return RuntimeValue(
        value.value.toString(),
        Type(
            type = Type.TypePrimitive.STRING,
            isNullable = isNullable
        )
    )
}

private fun castToDouble(value: RuntimeValue, isNullable: Boolean): RuntimeValue? {
    return when (value.type.type) {
        Type.TypePrimitive.INT -> RuntimeValue(
            value.getCastedValue<Int>().toDouble(),
            Type(
                type = Type.TypePrimitive.DOUBLE,
                isNullable = isNullable
            )
        )

        Type.TypePrimitive.DOUBLE -> RuntimeValue(
            value.value as Double,
            Type(
                type = Type.TypePrimitive.DOUBLE,
                isNullable = isNullable
            )
        )

        Type.TypePrimitive.STRING -> RuntimeValue(
            value.getCastedValue<String>().toDoubleOrNull() ?: return null,
            Type(
                type = Type.TypePrimitive.DOUBLE,
                isNullable = isNullable
            )
        )

        else -> null
    }
}

private fun castToInt(value: RuntimeValue, isNullable: Boolean): RuntimeValue? {
    return when (value.type.type) {
        Type.TypePrimitive.INT -> RuntimeValue(
            value,
            Type(
                type = Type.TypePrimitive.INT,
                isNullable = isNullable
            )
        )

        Type.TypePrimitive.DOUBLE -> RuntimeValue(
            value.getCastedValue<Double>().toInt(),
            Type(
                type = Type.TypePrimitive.INT,
                isNullable = isNullable
            )
        )

        Type.TypePrimitive.STRING -> RuntimeValue(
            value.getCastedValue<String>().toIntOrNull() ?: return null,
            Type(
                type = Type.TypePrimitive.INT,
                isNullable = isNullable
            )
        )

        else -> null
    }
}

private fun castToBool(value: RuntimeValue, isNullable: Boolean): RuntimeValue? {
    return when (value.type.type) {
        Type.TypePrimitive.BOOL -> RuntimeValue(
            value,
            Type(
                type = Type.TypePrimitive.BOOL,
                isNullable = isNullable
            )
        )

        Type.TypePrimitive.STRING -> RuntimeValue(
            value.getCastedValue<String>().toBoolean(),
            Type(
                type = Type.TypePrimitive.BOOL,
                isNullable = isNullable
            )
        )

        else -> null
    }
}
