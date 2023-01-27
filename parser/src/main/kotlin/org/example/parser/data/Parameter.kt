package org.example.parser.data

open class Parameter(
    val name: String,
    val type: Type
) {
    override fun toString(): String {
        return "Parameter(name=$name, type=$type)"
    }
}

