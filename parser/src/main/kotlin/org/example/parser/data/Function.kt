package org.example.parser.data

data class Function(
    val name: String,
    val parameters: List<Parameter>,
    val returnType: Type?,
    val body: List<Statement>
)