package org.example.parser.data

import java.util.Locale


data class Type(
    val type: TypePrimitive,
    val isNullable: Boolean,
) {
    enum class TypePrimitive {
        INT,
        DOUBLE,
        STRING,
        BOOL,
        VOID,
    }

    override fun toString(): String {
        return "${type.name.lowercase(Locale.US)}${if (isNullable) "?" else ""}"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Type

        if (type != other.type) return false
        return isNullable || !other.isNullable
    }

    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + isNullable.hashCode()
        return result
    }


}
