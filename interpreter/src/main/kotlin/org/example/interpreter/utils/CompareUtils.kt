package org.example.interpreter.utils

import org.example.parser.data.ComparisonOperator
import org.example.parser.data.RuntimeValue
import org.example.parser.data.Type

fun RuntimeValue.compare(other: RuntimeValue, operator: ComparisonOperator): Boolean? {
    if (this.type != other.type) {
        return null
    }
    return when (operator) {
        is ComparisonOperator.Equal -> compareEqual(this, other)
        is ComparisonOperator.NotEqual -> compareNotEqual(this, other)
        else -> compareNumbers(this, other, operator)
    }
}

private fun compareEqual(left: RuntimeValue, right: RuntimeValue): Boolean {
    return when (left.type.type) {
        Type.TypePrimitive.INT -> left.getCastedValue<Int>() == right.getCastedValue<Int>()
        Type.TypePrimitive.DOUBLE -> left.getCastedValue<Double>() == right.getCastedValue<Double>()
        Type.TypePrimitive.STRING -> left.getCastedValue<String>() == right.getCastedValue<String>()
        Type.TypePrimitive.BOOL -> left.getCastedValue<Boolean>() == right.getCastedValue<Boolean>()
        Type.TypePrimitive.VOID -> right.value == null
    }
}

private fun compareNotEqual(left: RuntimeValue, right: RuntimeValue): Boolean {
    return when (left.type.type) {
        Type.TypePrimitive.INT -> left.getCastedValue<Int>() != right.getCastedValue<Int>()
        Type.TypePrimitive.DOUBLE -> left.getCastedValue<Double>() != right.getCastedValue<Double>()
        Type.TypePrimitive.STRING -> left.getCastedValue<String>() != right.getCastedValue<String>()
        Type.TypePrimitive.BOOL -> left.getCastedValue<Boolean>() != right.getCastedValue<Boolean>()
        Type.TypePrimitive.VOID -> right.value != null
    }
}

fun compareNumbers(left: RuntimeValue, right: RuntimeValue, operator: ComparisonOperator): Boolean? {
    if (!left.isNonNullableNumber()) {
        return null
    }
    val leftValue = when (left.type.type) {
        Type.TypePrimitive.INT -> left.getCastedValue<Int>().toDouble()
        else -> left.getCastedValue()
    }
    val rightValue = when (left.type.type) {
        Type.TypePrimitive.INT -> right.getCastedValue<Int>().toDouble()
        else -> right.getCastedValue()
    }
    return when (operator) {
        ComparisonOperator.Greater -> leftValue > rightValue
        ComparisonOperator.GreaterOrEqual -> leftValue >= rightValue
        ComparisonOperator.Less -> leftValue < rightValue
        else -> leftValue <= rightValue
    }
}
