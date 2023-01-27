package org.example.parser.data

data class RuntimeValue(
    val value: Any?,
    val type: Type
) {
    fun <T> getCastedValue(): T {
        return value as T
    }
}
