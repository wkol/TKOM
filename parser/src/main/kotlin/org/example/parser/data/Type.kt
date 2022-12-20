package org.example.parser.data

data class Type(
    val type: TypePrimitive,
    val isNullable: Boolean,
) {
    enum class TypePrimitive {
        INT,
        DOUBLE,
        STRING,
        BOOL,
        VOID
    }
}